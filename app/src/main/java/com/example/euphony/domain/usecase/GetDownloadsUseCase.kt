package com.example.euphony.domain.usecase

import com.example.euphony.data.download.DownloadManager
import com.example.euphony.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

class GetDownloadsUseCase(
    private val downloadManager: DownloadManager
) {
    operator fun invoke(): Flow<List<DownloadEntity>> {
        return downloadManager.getAllDownloads()
    }
}

