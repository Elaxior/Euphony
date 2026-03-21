package com.example.euphony.domain.repository

import com.example.euphony.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerRepository {
    /**
     * Current playback state
     */
    val isPlaying: StateFlow<Boolean>

    /**
     * Current loading state
     */
    val isLoading: StateFlow<Boolean>

    /**
     * Current song being played
     */
    val currentSong: StateFlow<Song?>

    /**
     * Error message if any
     */
    val error: StateFlow<String?>

    /**
     * Current playback position in milliseconds
     */
    val currentPosition: StateFlow<Long>

    /**
     * Total duration in milliseconds
     */
    val duration: StateFlow<Long>

    /**
     * Play a song
     */
    suspend fun playSong(song: Song)

    /**
     * Pause playback
     */
    fun pause()

    /**
     * Resume playback
     */
    fun resume()

    /**
     * Stop playback and release resources
     */
    fun stop()

    /**
     * Seek to position
     */
    fun seekTo(positionMs: Long)

    /**
     * Get the underlying ExoPlayer instance
     */
    fun getExoPlayer(): androidx.media3.exoplayer.ExoPlayer?

    /**
     * Check if player is prepared
     */
    fun isReady(): Boolean
}
