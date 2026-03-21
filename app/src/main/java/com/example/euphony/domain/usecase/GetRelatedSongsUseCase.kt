package com.example.euphony.domain.usecase

import com.example.euphony.domain.model.Song
import com.example.euphony.domain.repository.MusicRepository

class GetRelatedSongsUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(songId: String): Result<List<Song>> {
        // This will use the related songs from YouTube
        // For now, return empty as we'll implement this when needed
        return Result.success(emptyList())
    }
}
