package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow

class GetAllPlaylistsUseCase(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Playlist>> {
        return libraryRepository.getAllPlaylists()
    }
}
