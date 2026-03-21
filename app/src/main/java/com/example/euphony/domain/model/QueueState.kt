package com.example.euphony.domain.model

data class QueueState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val playbackMode: PlaybackMode = PlaybackMode()
) {
    val hasNext: Boolean
        get() = currentIndex < queue.size - 1

    val hasPrevious: Boolean
        get() = currentIndex > 0
}
