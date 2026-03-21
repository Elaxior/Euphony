package com.example.euphony.domain.usecase

import com.example.euphony.domain.repository.PlayerRepository

class PauseSongUseCase(
    private val playerRepository: PlayerRepository
) {
    operator fun invoke() {
        playerRepository.pause()
    }
}
