package com.example.euphony.data.repository

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.euphony.data.download.DownloadManager
import com.example.euphony.data.player.MusicPlayer
import com.example.euphony.data.remote.AudioStreamExtractor
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExoPlayerRepository(
    context: Context,
    private val audioStreamExtractor: AudioStreamExtractor,
    private val downloadManager: DownloadManager
) : PlayerRepository {

    companion object {
        private const val TAG = "ExoPlayerRepository"
    }

    private val musicPlayer = MusicPlayer(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var onSongCompletedCallback: (() -> Unit)? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    override val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override val isPlaying: StateFlow<Boolean> = musicPlayer.isPlaying
    override val isLoading: StateFlow<Boolean> = musicPlayer.isLoading
    override val currentPosition: StateFlow<Long> = musicPlayer.currentPosition
    override val duration: StateFlow<Long> = musicPlayer.duration

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var playbackDelayed = false
    private var resumeOnFocusGain = false

    private var isDucking = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Restore volume if we were ducking
                if (isDucking) {
                    getExoPlayer()?.volume = 1.0f
                    isDucking = false
                }
                if (resumeOnFocusGain) {
                    resume()
                    resumeOnFocusGain = false
                }
                hasAudioFocus = true
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                resumeOnFocusGain = false
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                if (isPlaying.value) {
                    resumeOnFocusGain = true
                    pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Duck volume instead of pausing (correct behavior for Android 16)
                isDucking = true
                getExoPlayer()?.volume = 0.2f
            }
        }
    }

    init {
        musicPlayer.setOnSongEndedCallback {
            onSongCompletedCallback?.invoke()
        }
        configurePlayerCommands()
    }

    private fun configurePlayerCommands() {
        getExoPlayer()?.let { player ->
            player.setMediaItems(emptyList())
            // Register commands to ensure Media3 and System UI know what we support
            val availableCommands = Player.Commands.Builder()
                .addAll(
                    Player.COMMAND_PLAY_PAUSE,
                    Player.COMMAND_PREPARE,
                    Player.COMMAND_STOP,
                    Player.COMMAND_SEEK_TO_DEFAULT_POSITION,
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                    Player.COMMAND_SEEK_BACK,
                    Player.COMMAND_SEEK_FORWARD,
                    Player.COMMAND_GET_CURRENT_MEDIA_ITEM,
                    Player.COMMAND_GET_TIMELINE,
                    Player.COMMAND_GET_METADATA
                )
                .build()
        }
    }

    fun setOnSongCompletedCallback(callback: () -> Unit) {
        onSongCompletedCallback = callback
    }

    override suspend fun playSong(song: Song) {
        try {
            // Clear any previous errors
            _error.value = null

            // Set the current song FIRST so UI shows what we're trying to play
            _currentSong.value = song

            // Stop the previous song IMMEDIATELY to prevent auto-play conflicts
            musicPlayer.stop()

            if (!requestAudioFocus()) {
                _error.value = "Could not gain audio focus. Check your device's audio settings."
                // Don't clear currentSong - keep it visible with error
                return
            }

            // FIRST: Check if song is downloaded locally
            val downloadedSong = downloadManager.getDownload(song.videoId)

            if (downloadedSong != null && downloadedSong.downloadStatus == com.example.euphony.data.local.entity.DownloadStatus.COMPLETED) {
                // Play from local file
                Log.d(TAG, "Found downloaded song: ${downloadedSong.filePath}")
                val localFile = java.io.File(downloadedSong.filePath)

                if (localFile.exists()) {
                    try {
                        // Use file:// URI scheme for local files
                        val fileUri = "file://${localFile.absolutePath}"
                        Log.d(TAG, "✅ Playing offline from: $fileUri (size: ${localFile.length()} bytes)")
                        musicPlayer.prepare(fileUri)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to play local file: ${e.message}", e)
                        _error.value = "Failed to play offline file. Streaming instead..."
                        streamSong(song)
                    }
                } else {
                    // File doesn't exist, fall back to streaming
                    Log.w(TAG, "Local file not found at ${downloadedSong.filePath}, falling back to streaming")
                    _error.value = "Offline file not found. Streaming instead..."
                    streamSong(song)
                }
            } else {
                // Not downloaded, stream from YouTube
                Log.d(TAG, "Song not downloaded, streaming from YouTube")
                streamSong(song)
            }
        } catch (e: Exception) {
            // Keep the song visible but show error
            _error.value = "Failed to play song: ${e.message ?: "Unknown error"}. Tap retry."
            Log.e(TAG, "Error playing song", e)
            abandonAudioFocus()
        }
    }

    private suspend fun streamSong(song: Song) {
        audioStreamExtractor.getAudioStreamUrl(song.videoId)
            .onSuccess { streamUrl ->
                // Successfully got stream URL, now prepare the player
                Log.d(TAG, "Streaming: ${song.title}")
                musicPlayer.prepare(streamUrl)
            }
            .onFailure { exception ->
                // Keep the song visible but show error
                _error.value = "Failed to load audio: ${exception.message ?: "YouTube API error"}. Tap retry or try another song."
                Log.e(TAG, "Stream extraction failed", exception)
                // DON'T clear currentSong - let user see what failed and retry
                abandonAudioFocus()
            }
    }

    override fun pause() {
        musicPlayer.pause()
    }

    override fun resume() {
        if (requestAudioFocus()) {
            musicPlayer.resume()
        }
    }

    override fun stop() {
        musicPlayer.stop()
        _currentSong.value = null
        _error.value = null
        abandonAudioFocus()
    }

    override fun seekTo(positionMs: Long) {
        musicPlayer.seekTo(positionMs)
    }

    override fun getExoPlayer(): ExoPlayer? {
        return musicPlayer.getPlayer()
    }

    override fun isReady(): Boolean {
        return musicPlayer.isReady()
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setWillPauseWhenDucked(false) // We handle ducking manually with volume
                .setAcceptsDelayedFocusGain(true) // Android 16: accept delayed grant
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
            // Focus was delayed — still allow playback setup; will resume when focus arrives
            playbackDelayed = true
        }
        return hasAudioFocus || result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }

        hasAudioFocus = false
        resumeOnFocusGain = false
    }

    fun release() {
        abandonAudioFocus()
        musicPlayer.release()
    }
}