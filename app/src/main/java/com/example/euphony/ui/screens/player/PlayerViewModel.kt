package com.example.euphony.ui.screens.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.euphony.data.download.DownloadManager
import com.example.euphony.data.repository.FakeQueueRepository
import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.QueueState
import com.example.euphony.domain.repository.LibraryRepository
import com.example.euphony.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlayerUiState(
    val queueState: QueueState = QueueState(),
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Int? = null, // 0-100 or null if not downloading
    val playlists: List<Playlist> = emptyList(),
    val showAddToPlaylistDialog: Boolean = false,
    val showCreatePlaylistDialog: Boolean = false
)

class PlayerViewModel(
    private val getQueueStateUseCase: GetQueueStateUseCase,
    private val pauseSongUseCase: PauseSongUseCase,
    private val resumeSongUseCase: ResumeSongUseCase,
    private val playNextSongUseCase: PlayNextSongUseCase,
    private val playPreviousSongUseCase: PlayPreviousSongUseCase,
    private val seekToPositionUseCase: SeekToPositionUseCase,
    private val toggleShuffleUseCase: ToggleShuffleUseCase,
    private val cycleRepeatModeUseCase: CycleRepeatModeUseCase,
    private val queueRepository: FakeQueueRepository,
    // NEW: Library functionality
    private val libraryRepository: LibraryRepository,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    // NEW: Download functionality
    private val downloadManager: DownloadManager
) : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observeQueue()
        observeCurrentSongFavoriteStatus()
        observeCurrentSongDownloadStatus()
        observeDownloadProgress()
        observePlaylists()
    }

    private fun observeQueue() {
        viewModelScope.launch {
            getQueueStateUseCase().collect { queueState ->
                _uiState.update { it.copy(queueState = queueState) }
            }
        }
    }

    // NEW: Observe if current song is favorited
    private fun observeCurrentSongFavoriteStatus() {
        viewModelScope.launch {
            _uiState
                .map { it.queueState.currentSong?.videoId }
                .distinctUntilChanged()
                .collectLatest { videoId ->
                    if (videoId != null) {
                        libraryRepository.isFavorite(videoId).collect { isFav ->
                            _uiState.update { it.copy(isFavorite = isFav) }
                        }
                    } else {
                        _uiState.update { it.copy(isFavorite = false) }
                    }
                }
        }
    }

    // NEW: Observe playlists for dialog
    private fun observePlaylists() {
        viewModelScope.launch {
            libraryRepository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    // NEW: Observe download status
    private fun observeCurrentSongDownloadStatus() {
        viewModelScope.launch {
            _uiState
                .map { it.queueState.currentSong?.videoId }
                .distinctUntilChanged()
                .collectLatest { videoId ->
                    if (videoId != null) {
                        downloadManager.isDownloaded(videoId).collect { isDownloaded ->
                            _uiState.update { it.copy(isDownloaded = isDownloaded) }
                        }
                    } else {
                        _uiState.update { it.copy(isDownloaded = false) }
                    }
                }
        }
    }

    // NEW: Observe download progress
    private fun observeDownloadProgress() {
        viewModelScope.launch {
            downloadManager.downloadProgress.collect { progressMap ->
                val currentVideoId = _uiState.value.queueState.currentSong?.videoId
                val progress = if (currentVideoId != null) {
                    progressMap[currentVideoId]
                } else {
                    null
                }
                _uiState.update { it.copy(downloadProgress = progress) }
            }
        }
    }

    // ========== PLAYBACK CONTROLS ==========

    fun togglePlayPause() {
        val isPlaying = _uiState.value.queueState.isPlaying
        if (isPlaying) {
            pauseSongUseCase()
        } else {
            resumeSongUseCase()
        }
    }

    fun pause() {
        pauseSongUseCase()
    }

    fun resume() {
        resumeSongUseCase()
    }

    fun playNext() {
        viewModelScope.launch {
            playNextSongUseCase()
        }
    }

    fun playPrevious() {
        viewModelScope.launch {
            playPreviousSongUseCase()
        }
    }

    fun seekTo(positionMs: Long) {
        seekToPositionUseCase(positionMs)
    }

    fun toggleShuffle() {
        toggleShuffleUseCase()
    }

    fun cycleRepeatMode() {
        cycleRepeatModeUseCase()
    }

    fun playQueueSong(index: Int) {
        viewModelScope.launch {
            queueRepository.playFromQueue(index)
        }
    }

    // ========== RETRY FUNCTIONALITY ==========

    fun retrySong() {
        viewModelScope.launch {
            try {
                _uiState.value.queueState.currentSong?.let { currentSong ->
                    Log.d(TAG, "Retrying song: ${currentSong.title}")
                    queueRepository.playSong(currentSong)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying song", e)
            }
        }
    }

    // ========== NEW: LIBRARY ACTIONS ==========

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                // FIX: Use let to handle nullable Song
                _uiState.value.queueState.currentSong?.let { currentSong ->
                    if (_uiState.value.isFavorite) {
                        libraryRepository.removeFromFavorites(currentSong.videoId)
                        Log.d(TAG, "Removed from favorites: ${currentSong.title}")
                    } else {
                        libraryRepository.addToFavorites(currentSong)
                        Log.d(TAG, "Added to favorites: ${currentSong.title}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    fun showAddToPlaylistDialog() {
        _uiState.update { it.copy(showAddToPlaylistDialog = true) }
    }

    fun hideAddToPlaylistDialog() {
        _uiState.update { it.copy(showAddToPlaylistDialog = false) }
    }

    fun showCreatePlaylistDialog() {
        _uiState.update { it.copy(showCreatePlaylistDialog = true) }
    }

    fun hideCreatePlaylistDialog() {
        _uiState.update { it.copy(showCreatePlaylistDialog = false) }
    }

    fun addCurrentSongToPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                // FIX: Use let to handle nullable Song
                _uiState.value.queueState.currentSong?.let { currentSong ->
                    addSongToPlaylistUseCase(playlistId, currentSong)
                    _uiState.update { it.copy(showAddToPlaylistDialog = false) }
                    Log.d(TAG, "Added ${currentSong.title} to playlist $playlistId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding song to playlist", e)
            }
        }
    }

    fun createPlaylistAndAddSong(playlistName: String) {
        viewModelScope.launch {
            try {
                // FIX: Use let to handle nullable Song
                _uiState.value.queueState.currentSong?.let { currentSong ->
                    val playlistId = createPlaylistUseCase(playlistName)
                    addSongToPlaylistUseCase(playlistId, currentSong)
                    _uiState.update { it.copy(showCreatePlaylistDialog = false) }
                    Log.d(TAG, "Created playlist '$playlistName' and added song")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating playlist", e)
            }
        }
    }

    // ========== DOWNLOAD FUNCTIONALITY ==========

    fun downloadSong() {
        viewModelScope.launch {
            try {
                _uiState.value.queueState.currentSong?.let { currentSong ->
                    Log.d(TAG, "Starting download: ${currentSong.title}")
                    downloadManager.downloadSong(currentSong)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading song", e)
            }
        }
    }

}