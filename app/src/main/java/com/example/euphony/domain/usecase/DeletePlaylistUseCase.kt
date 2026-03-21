package com.example.euphony.domain.usecase

import com.example.euphony.domain.repository.LibraryRepository

class DeletePlaylistUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(playlistId: Long) {
        libraryRepository.deletePlaylist(playlistId)
    }
}
