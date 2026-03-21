package com.example.euphony.domain.usecase

import com.example.euphony.data.download.DownloadManager
import com.example.euphony.domain.model.Song

class DownloadSongUseCase(
    private val downloadManager: DownloadManager
) {
    suspend operator fun invoke(song: Song) {
        downloadManager.downloadSong(song)
    }
}

