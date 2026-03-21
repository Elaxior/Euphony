package com.example.euphony.domain.usecase

import com.example.euphony.domain.repository.LibraryRepository

class RenamePlaylistUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(playlistId: Long, newName: String) {
        require(newName.isNotBlank()) { "Playlist name cannot be empty" }
        libraryRepository.renamePlaylist(playlistId, newName)
    }
}
