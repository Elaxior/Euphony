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
    get() = queue.isNotEmpty() && (
        currentIndex < queue.size - 1 || playbackMode.repeatMode == RepeatMode.REPEAT_ALL
        )

    val hasPrevious: Boolean
    get() = queue.isNotEmpty() && (
        currentIndex > 0 || playbackMode.repeatMode == RepeatMode.REPEAT_ALL
        )
}
