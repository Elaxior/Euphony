package com.example.euphony.domain.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: Long, // in milliseconds
    val thumbnailUrl: String = "",
    val videoId: String = "" // for YouTube integration later
)
