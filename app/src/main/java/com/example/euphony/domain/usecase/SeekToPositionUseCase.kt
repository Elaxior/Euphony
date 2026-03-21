package com.example.euphony.domain.usecase

import com.example.euphony.domain.repository.PlayerRepository

class SeekToPositionUseCase(
    private val playerRepository: PlayerRepository
) {
    operator fun invoke(positionMs: Long) {
        playerRepository.seekTo(positionMs)
    }
}
