package com.example.euphony.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table for many-to-many relationship between Playlist and Song
 * Follows Room best practices with composite primary key
 */
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "videoId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE // Delete entries when playlist is deleted
        )
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["videoId"])
    ]
)
data class PlaylistSongCrossRefEntity(
    val playlistId: Long,
    val videoId: String,
    val addedAt: Long = System.currentTimeMillis(),
    val position: Int = 0 // For maintaining song order in playlist
)
