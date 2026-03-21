package com.example.euphony.data.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayer(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var onSongEndedCallback: (() -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()

            exoPlayer = ExoPlayer.Builder(context)
                .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ false) // We handle focus manually
                .setHandleAudioBecomingNoisy(true) // Pause on headphone disconnect
                .setWakeMode(C.WAKE_MODE_NETWORK) // Keep CPU/network alive during playback (Android 16 compatible)
                .build()
                .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _isLoading.value = playbackState == Player.STATE_BUFFERING
                        _isPlaying.value = playbackState == Player.STATE_READY && playWhenReady

                        if (playbackState == Player.STATE_READY) {
                            _duration.value = duration
                            startProgressUpdate()
                        } else if (playbackState == Player.STATE_ENDED) {
                            stopProgressUpdate()
                            // Trigger auto-play (AppContainer handles the delay/debounce)
                            onSongEndedCallback?.invoke()
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (isPlaying) {
                            startProgressUpdate()
                        } else {
                            stopProgressUpdate()
                        }
                    }
                })
            }
        }
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressRunnable = object : Runnable {
            override fun run() {
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(progressRunnable!!)
    }

    private fun stopProgressUpdate() {
        progressRunnable?.let { handler.removeCallbacks(it) }
    }

    fun setOnSongEndedCallback(callback: () -> Unit) {
        onSongEndedCallback = callback
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun prepare(url: String) {
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun stop() {
        exoPlayer?.apply {
            stop()
            clearMediaItems()
        }
        stopProgressUpdate()
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun release() {
        stopProgressUpdate()
        exoPlayer?.release()
        exoPlayer = null
    }

    fun isReady(): Boolean {
        return exoPlayer?.playbackState == Player.STATE_READY
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }
}