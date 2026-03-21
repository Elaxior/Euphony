package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow

class GetFavoritesUseCase(
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return libraryRepository.getFavorites()
    }
}
