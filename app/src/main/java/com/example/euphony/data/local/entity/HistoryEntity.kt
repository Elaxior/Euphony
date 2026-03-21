package com.example.euphony.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listening_history")
data class HistoryEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: Long,
    val playedAt: Long = System.currentTimeMillis()
)
