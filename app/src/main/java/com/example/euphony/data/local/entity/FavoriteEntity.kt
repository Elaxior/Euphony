package com.example.euphony.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a song in the library
 * Can be a favorite, in a playlist, or both
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: Long,
    val isFavorite: Boolean = false, // True if user marked as favorite
    val addedAt: Long = System.currentTimeMillis()
)
