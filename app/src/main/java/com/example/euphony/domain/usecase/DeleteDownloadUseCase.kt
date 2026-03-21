package com.example.euphony.domain.usecase

import com.example.euphony.data.download.DownloadManager

class DeleteDownloadUseCase(
    private val downloadManager: DownloadManager
) {
    suspend operator fun invoke(videoId: String) {
        downloadManager.deleteDownload(videoId)
    }
}

