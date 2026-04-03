package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.QueueRepository

class AddToQueueNextUseCase(
    private val queueRepository: QueueRepository
) {
    suspend operator fun invoke(song: Song) {
        queueRepository.addNextToQueue(song)
    }
}
