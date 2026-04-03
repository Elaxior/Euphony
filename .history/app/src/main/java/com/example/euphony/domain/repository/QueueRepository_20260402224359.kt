package com.example.euphony.domain.repository

import com.example.euphony.domain.model.QueueState
import com.example.euphony.domain.model.RepeatMode
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface QueueRepository {
    /**
     * Get current queue state
     */
    fun getQueueState(): Flow<QueueState>

    /**
     * Add a song to the end of the queue
     */
    suspend fun addToQueue(song: Song)

    /**
     * Add a song right after the currently playing song
     */
    suspend fun addNextToQueue(song: Song)

    /**
     * Play a single song (replaces queue)
     */
    suspend fun playSong(song: Song)

    /**
     * Play a list of songs starting at index
     */
    suspend fun playQueue(songs: List<Song>, startIndex: Int = 0)

    /**
     * Clear the entire queue
     */
    suspend fun clearQueue()

    /**
     * Toggle shuffle mode
     */
    fun toggleShuffle()

    /**
     * Cycle through repeat modes
     */
    fun cycleRepeatMode()

    /**
     * Set specific repeat mode
     */
    fun setRepeatMode(mode: RepeatMode)

    /**
     * Add related songs to queue
     */
    suspend fun addRelatedSongs(baseSong: Song)
}
