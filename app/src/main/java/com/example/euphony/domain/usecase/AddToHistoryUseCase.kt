package com.example.euphony.domain.usecase

import com.example.euphony.data.local.dao.HistoryDao
import com.example.euphony.data.local.entity.HistoryEntity
import com.example.euphony.domain.model.Song

/**
 * Adds a song to listening history
 */
class AddToHistoryUseCase(
    private val historyDao: HistoryDao
) {
    suspend operator fun invoke(song: Song) {
        val historyEntity = HistoryEntity(
            videoId = song.videoId,
            title = song.title,
            artist = song.artist,
            thumbnailUrl = song.thumbnailUrl,
            duration = song.duration,
            playedAt = System.currentTimeMillis()
        )
        historyDao.insertHistory(historyEntity)
    }
}
