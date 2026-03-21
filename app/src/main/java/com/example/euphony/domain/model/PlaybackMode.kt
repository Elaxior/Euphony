package com.example.euphony.domain.model

enum class RepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE
}

data class PlaybackMode(
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)
