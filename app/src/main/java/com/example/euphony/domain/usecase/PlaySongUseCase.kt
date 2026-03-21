package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.QueueRepository

class PlaySongUseCase(
    private val queueRepository: QueueRepository,
    private val addToHistoryUseCase: AddToHistoryUseCase
) {
    suspend operator fun invoke(song: Song) {
        // Add to listening history in background
        try {
            addToHistoryUseCase(song)
        } catch (e: Exception) {
            // Don't fail if history tracking fails
            android.util.Log.e("PlaySongUseCase", "Failed to add to history", e)
        }

        // Play the song
        queueRepository.playSong(song)
    }
}
