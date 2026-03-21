package com.example.euphony.domain.usecase

import com.example.euphony.data.repository.FakeQueueRepository

class PlayPreviousSongUseCase(
    private val queueRepository: FakeQueueRepository
) {
    suspend operator fun invoke(): Boolean {
        return if (queueRepository.canPlayPrevious()) {
            queueRepository.playPrevious()
            true
        } else {
            false
        }
    }
}
