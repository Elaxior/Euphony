package com.example.euphony.data.repository

import android.util.Log
import com.example.euphony.di.AppContainer
import com.example.euphony.data.remote.RelatedSongsFetcher
import com.example.euphony.domain.model.PlaybackMode
import com.example.euphony.domain.model.QueueState
import com.example.euphony.domain.model.RepeatMode
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.PlayerRepository
import com.example.euphony.domain.repository.QueueRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FakeQueueRepository(
    private val playerRepository: PlayerRepository,
    private val relatedSongsFetcher: RelatedSongsFetcher
) : QueueRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "FakeQueueRepository"

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    private val _currentIndex = MutableStateFlow(-1)
    private val _playbackMode = MutableStateFlow(PlaybackMode())
    private val _originalQueue = MutableStateFlow<List<Song>>(emptyList())
    private var isFixedQueue = false // True when playing favorites/playlists (don't auto-add songs)

    private var lastManualPlayTime = 0L

    fun prepareManualInteraction() {
        lastManualPlayTime = System.currentTimeMillis()
        // === THE FIX ===
        // Explicitly tell AppContainer to KILL any pending auto-play timer.
        // This ensures the 500ms delay we added in AppContainer gets cancelled
        // before it can trigger "Play Next".
        AppContainer.cancelPendingAutoPlay()
    }

    override fun getQueueState(): Flow<QueueState> {
        val baseState = combine(
            playerRepository.currentSong,
            playerRepository.isPlaying,
            playerRepository.isLoading,
            playerRepository.error,
            _queue
        ) { currentSong, isPlaying, isLoading, error, queue ->
            QueueState(
                currentSong = currentSong,
                queue = queue,
                currentIndex = _currentIndex.value,
                isPlaying = isPlaying,
                isLoading = isLoading,
                error = error,
                playbackMode = _playbackMode.value
            )
        }

        return baseState
            .combine(_currentIndex) { state, currentIndex ->
                state.copy(currentIndex = currentIndex)
            }
            .combine(_playbackMode) { state, playbackMode ->
                state.copy(playbackMode = playbackMode)
            }
    }

    override suspend fun addToQueue(song: Song) {
        val currentQueue = _queue.value
        _queue.value = currentQueue + song
    }

    override suspend fun playSong(song: Song) {
        prepareManualInteraction()

        val currentQueue = _queue.value
        val existingIndex = currentQueue.indexOfFirst { it.videoId == song.videoId }

        if (existingIndex != -1) {
            playFromQueue(existingIndex)
            // Only add related songs if not in fixed queue mode
            if (!isFixedQueue && existingIndex >= currentQueue.size - 3) {
                scope.launch { addRelatedSongs(song) }
            }
        } else {
            isFixedQueue = false // Single song = radio mode, will auto-add related
            _queue.value = listOf(song)
            _originalQueue.value = listOf(song)
            _currentIndex.value = 0
            playerRepository.playSong(song)
            scope.launch { addRelatedSongs(song) }
        }
    }

    override suspend fun playQueue(songs: List<Song>, startIndex: Int) {
        prepareManualInteraction()

        if (songs.isNotEmpty() && startIndex in songs.indices) {
            isFixedQueue = true // Playing a specific queue - don't auto-add songs
            _originalQueue.value = songs
            _queue.value = if (_playbackMode.value.isShuffleEnabled) {
                shuffleQueue(songs, startIndex)
            } else {
                songs
            }
            _currentIndex.value = if (_playbackMode.value.isShuffleEnabled) 0 else startIndex
            playerRepository.playSong(_queue.value[_currentIndex.value])

            // DON'T add related songs when playing a specific queue (like favorites or playlists)
            // The queue should stay as provided
            Log.d(TAG, "Playing fixed queue of ${songs.size} songs, starting at index $startIndex")
        }
    }

    override suspend fun clearQueue() {
        _queue.value = emptyList()
        _originalQueue.value = emptyList()
        _currentIndex.value = -1
        playerRepository.stop()
    }

    override fun toggleShuffle() {
        val currentMode = _playbackMode.value
        val newShuffleState = !currentMode.isShuffleEnabled

        if (_queue.value.isEmpty()) return

        _playbackMode.value = currentMode.copy(isShuffleEnabled = newShuffleState)
        val currentSong = _queue.value.getOrNull(_currentIndex.value)

        if (newShuffleState) {
            val currentIndexInOriginal = _originalQueue.value.indexOfFirst { it.id == currentSong?.id }
            val shuffled = shuffleQueue(_originalQueue.value, currentIndexInOriginal.takeIf { it >= 0 } ?: 0)
            _queue.value = shuffled
            _currentIndex.value = 0
        } else {
            _queue.value = _originalQueue.value
            _currentIndex.value = _originalQueue.value.indexOfFirst { it.id == currentSong?.id }.takeIf { it >= 0 } ?: 0
        }
    }

    override fun cycleRepeatMode() {
        val currentMode = _playbackMode.value.repeatMode
        val nextMode = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
        }
        _playbackMode.value = _playbackMode.value.copy(repeatMode = nextMode)
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _playbackMode.value = _playbackMode.value.copy(repeatMode = mode)
    }

    override suspend fun addRelatedSongs(baseSong: Song) {
        relatedSongsFetcher.getRelatedSongs(baseSong.videoId, limit = 80)
            .onSuccess { relatedSongs ->
                if (relatedSongs.isNotEmpty()) {
                    val currentQueue = _queue.value
                    val existingIds = currentQueue.map { it.videoId }.toSet()
                    val newSongs = relatedSongs.filterNot { it.videoId in existingIds }

                    val rankedSongs = rankRelatedSongs(baseSong, newSongs)
                        .take(30)

                    if (rankedSongs.isNotEmpty()) {
                        val updatedQueue = currentQueue + rankedSongs
                        _queue.value = updatedQueue
                        if (!_playbackMode.value.isShuffleEnabled) {
                            _originalQueue.value = updatedQueue
                        }
                    }
                }
            }
    }

    suspend fun playNext(isAutoPlay: Boolean = false) {
        val queue = _queue.value
        val currentIndex = _currentIndex.value
        val repeatMode = _playbackMode.value.repeatMode

        // For REPEAT_ONE in autoPlay mode, bypass the manual interaction guard
        // and immediately replay the same song — this IS the intended behaviour.
        if (isAutoPlay && repeatMode == RepeatMode.REPEAT_ONE) {
            if (currentIndex in queue.indices) {
                playerRepository.playSong(queue[currentIndex])
            }
            return
        }

        // For all other autoPlay cases, enforce the debounce guard
        if (isAutoPlay) {
            val timeSinceManual = System.currentTimeMillis() - lastManualPlayTime
            if (timeSinceManual < 2000) return
        } else {
            prepareManualInteraction()
        }

        when {
            currentIndex < queue.size - 1 -> {
                val nextIndex = currentIndex + 1
                _currentIndex.value = nextIndex
                playerRepository.playSong(queue[nextIndex])

                // Only add related songs if in radio mode (not fixed queue)
                if (!isFixedQueue && nextIndex >= queue.size - 3 && queue.isNotEmpty()) {
                    scope.launch { addRelatedSongs(queue[nextIndex]) }
                }
            }
            repeatMode == RepeatMode.REPEAT_ALL && queue.isNotEmpty() -> {
                // When wrapping around with shuffle enabled, re-shuffle the queue
                if (_playbackMode.value.isShuffleEnabled) {
                    val reshuffled = _originalQueue.value.shuffled()
                    _queue.value = reshuffled
                    _currentIndex.value = 0
                    playerRepository.playSong(reshuffled[0])
                } else {
                    _currentIndex.value = 0
                    playerRepository.playSong(queue[0])
                }
            }
            // Queue ended and no repeat — do nothing (natural end)
        }
    }

    suspend fun playPrevious(forceSkip: Boolean = false) {
        prepareManualInteraction()

        val queue = _queue.value
        val currentIndex = _currentIndex.value
        val repeatMode = _playbackMode.value.repeatMode
        val currentPosition = playerRepository.currentPosition.value

        Log.d(TAG, "Play previous. Index: $currentIndex, Force: $forceSkip, Position: $currentPosition")

        // If more than 3 seconds in and not force-skipping, restart the current song
        if (!forceSkip && currentPosition > 3000) {
            playerRepository.seekTo(0)
            return
        }

        when {
            currentIndex > 0 -> {
                val previousIndex = currentIndex - 1
                _currentIndex.value = previousIndex
                playerRepository.playSong(queue[previousIndex])
            }
            repeatMode == RepeatMode.REPEAT_ALL && queue.isNotEmpty() -> {
                // Wrap to end of queue
                val lastIndex = queue.size - 1
                _currentIndex.value = lastIndex
                playerRepository.playSong(queue[lastIndex])
            }
            else -> {
                // At first song, just restart it
                playerRepository.seekTo(0)
            }
        }
    }

    suspend fun playFromQueue(index: Int) {
        prepareManualInteraction()
        val queue = _queue.value
        if (index in queue.indices) {
            _currentIndex.value = index
            playerRepository.playSong(queue[index])
        }
    }

    fun canPlayNext(): Boolean = true
    fun canPlayPrevious(): Boolean = true

    fun getCurrentQueueStateSnapshot(): QueueState {
        val currentSong = playerRepository.currentSong.value
        val isPlaying = playerRepository.isPlaying.value
        val isLoading = playerRepository.isLoading.value
        val error = playerRepository.error.value

        return QueueState(
            currentSong = currentSong,
            queue = _queue.value,
            currentIndex = _currentIndex.value,
            isPlaying = isPlaying,
            isLoading = isLoading,
            error = error,
            playbackMode = _playbackMode.value
        )
    }

    private fun shuffleQueue(songs: List<Song>, currentSongIndex: Int): List<Song> {
        if (songs.isEmpty()) return emptyList()
        val currentSong = songs.getOrNull(currentSongIndex) ?: songs.first()
        val otherSongs = songs.filterNot { it.id == currentSong.id }.shuffled()
        return listOf(currentSong) + otherSongs
    }

    private fun rankRelatedSongs(baseSong: Song, candidates: List<Song>): List<Song> {
        if (candidates.isEmpty()) return emptyList()

        val baseArtist = normalizeText(baseSong.artist)
        val baseTokens = tokenize(baseSong.title) + tokenize(baseSong.artist)
        val artistCap = mutableMapOf<String, Int>()

        return candidates
            .distinctBy { it.videoId }
            .map { candidate -> candidate to scoreCandidate(baseArtist, baseTokens, candidate) }
            .sortedByDescending { it.second }
            .map { it.first }
            .filter { song ->
                val artist = normalizeText(song.artist)
                val currentCount = artistCap[artist] ?: 0
                val allowed = currentCount < 3
                if (allowed) {
                    artistCap[artist] = currentCount + 1
                }
                allowed
            }
    }

    private fun scoreCandidate(baseArtist: String, baseTokens: Set<String>, candidate: Song): Double {
        val candidateArtist = normalizeText(candidate.artist)
        val candidateTokens = tokenize(candidate.title) + tokenize(candidate.artist)

        val artistScore = when {
            candidateArtist == baseArtist -> 3.0
            candidateArtist.contains(baseArtist) || baseArtist.contains(candidateArtist) -> 1.5
            else -> 0.0
        }

        val sharedTokenCount = baseTokens.intersect(candidateTokens).size
        val tokenScore = sharedTokenCount * 0.6

        val durationSeconds = (candidate.duration / 1000L).toInt()
        val durationScore = if (durationSeconds in 120..420) 0.4 else 0.0

        return artistScore + tokenScore + durationScore
    }

    private fun tokenize(text: String): Set<String> {
        return normalizeText(text)
            .split(" ")
            .filter { it.length > 2 }
            .toSet()
    }

    private fun normalizeText(text: String): String {
        return text
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}