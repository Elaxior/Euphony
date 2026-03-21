package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.LibraryRepository

class AddSongToPlaylistUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(playlistId: Long, song: Song) {
        libraryRepository.addSongToPlaylist(playlistId, song)
    }
}
