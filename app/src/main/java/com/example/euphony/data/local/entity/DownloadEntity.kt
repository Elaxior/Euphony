package com.example.euphony.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a downloaded song
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: Long,
    val filePath: String, // Local file path to downloaded audio
    val fileSize: Long, // Size in bytes
    val downloadedAt: Long = System.currentTimeMillis(),
    val downloadStatus: DownloadStatus = DownloadStatus.COMPLETED
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED
}

