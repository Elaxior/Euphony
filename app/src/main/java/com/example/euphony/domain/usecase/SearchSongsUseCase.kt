package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.MusicRepository

class SearchSongsUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): Result<List<Song>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return musicRepository.searchSongs(query)
    }
}
