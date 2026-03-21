package com.example.euphony.domain.usecase

import com.example.euphony.domain.repository.LibraryRepository

class RemoveSongFromPlaylistUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(playlistId: Long, videoId: String) {
        libraryRepository.removeSongFromPlaylist(playlistId, videoId)
    }
}
