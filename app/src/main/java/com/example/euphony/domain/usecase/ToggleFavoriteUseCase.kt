package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.first

class ToggleFavoriteUseCase(
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(song: Song) {
        val isFavorite = libraryRepository.isFavorite(song.videoId).first()
        if (isFavorite) {
            libraryRepository.removeFromFavorites(song.videoId)
        } else {
            libraryRepository.addToFavorites(song)
        }
    }
}
