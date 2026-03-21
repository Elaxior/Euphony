package com.example.euphony.data.remote.mapper

import com.example.euphony.domain.model.Song
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object SongMapper {

    /**
     * Map StreamInfoItem to Song
     */
    fun StreamInfoItem.toSong(): Song {
        return Song(
            id = url.substringAfterLast("v=").substringBefore("&"),
            title = name ?: "Unknown Title",
            artist = uploaderName ?: "Unknown Artist",
            album = "",
            duration = duration * 1000L, // Convert seconds to milliseconds
            thumbnailUrl = thumbnails.firstOrNull()?.url ?: "",
            videoId = url.substringAfterLast("v=").substringBefore("&")
        )
    }

    /**
     * Map StreamInfo to Song (with more details)
     */
    fun StreamInfo.toSong(): Song {
        return Song(
            id = url.substringAfterLast("v=").substringBefore("&"),
            title = name ?: "Unknown Title",
            artist = uploaderName ?: "Unknown Artist",
            album = "",
            duration = duration * 1000L,
            thumbnailUrl = thumbnails.firstOrNull()?.url ?: "",
            videoId = url.substringAfterLast("v=").substringBefore("&")
        )
    }

    /**
     * Batch map list of StreamInfoItems to Songs
     */
    fun List<StreamInfoItem>.toSongs(): List<Song> {
        return this.map { it.toSong() }
    }
}
