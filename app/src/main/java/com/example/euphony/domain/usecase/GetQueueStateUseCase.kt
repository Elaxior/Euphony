package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.QueueState
import com.example.euphony.domain.repository.QueueRepository
import kotlinx.coroutines.flow.Flow

class GetQueueStateUseCase(
    private val queueRepository: QueueRepository
) {
    operator fun invoke(): Flow<QueueState> {
        return queueRepository.getQueueState()
    }
}
