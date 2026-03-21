package com.example.euphony.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song>,
    val thumbnailUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
