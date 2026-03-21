package com.example.euphony.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * POJO for Room @Relation query
 * Fetches a playlist with all its songs in one query
 */
data class PlaylistWithSongs(
    @Embedded
    val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "videoId",
        associateBy = Junction(
            value = PlaylistSongCrossRefEntity::class,
            parentColumn = "playlistId",
            entityColumn = "videoId"
        )
    )
    val songs: List<FavoriteEntity> // Reusing FavoriteEntity structure for songs
)
