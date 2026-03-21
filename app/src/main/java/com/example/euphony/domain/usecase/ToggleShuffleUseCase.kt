package com.example.euphony.domain.usecase

import com.example.euphony.data.repository.FakeQueueRepository

class ToggleShuffleUseCase(
    private val queueRepository: FakeQueueRepository
) {
    operator fun invoke() {
        queueRepository.toggleShuffle()
    }
}
