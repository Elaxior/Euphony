package com.example.euphony.domain.usecase

import com.example.euphony.domain.repository.LibraryRepository

class CreatePlaylistUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(name: String): Long {
        require(name.isNotBlank()) { "Playlist name cannot be empty" }
        return libraryRepository.createPlaylist(name)
    }
}
