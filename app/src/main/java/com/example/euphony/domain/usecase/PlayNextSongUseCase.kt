package com.example.euphony.domain.usecase

import com.example.euphony.data.repository.FakeQueueRepository

class PlayNextSongUseCase(
    private val queueRepository: FakeQueueRepository
) {
    suspend operator fun invoke(): Boolean {
        return if (queueRepository.canPlayNext()) {
            queueRepository.playNext()
            true
        } else {
            false
        }
    }
}
