package com.example.euphony.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubeDataSource {

    /**
     * Search for songs on YouTube
     */
    suspend fun searchSongs(query: String): Result<List<StreamInfoItem>> = withContext(Dispatchers.IO) {
        try {
            val youtubeService = NewPipe.getService(0) // YouTube service
            val searchExtractor = youtubeService.getSearchExtractor(query)
            searchExtractor.fetchPage()

            // Filter only audio/video streams (not channels or playlists)
            val results = searchExtractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .filter { it.streamType != null }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get related songs for a given video ID
     */
    suspend fun getRelatedSongs(videoId: String): Result<List<StreamInfoItem>> = withContext(Dispatchers.IO) {
        try {
            val youtubeService = NewPipe.getService(0) // YouTube service
            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(youtubeService, videoUrl)

            val relatedItems = streamInfo.relatedItems
                .filterIsInstance<StreamInfoItem>()
                .filter { it.streamType != null }

            Result.success(relatedItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get detailed info about a specific song
     */
    suspend fun getSongInfo(videoId: String): Result<StreamInfo> = withContext(Dispatchers.IO) {
        try {
            val youtubeService = NewPipe.getService(0) // YouTube service
            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(youtubeService, videoUrl)
            Result.success(streamInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get popular music (using search instead of trending to avoid API issues)
     */
    suspend fun getTrendingSongs(): Result<List<StreamInfoItem>> = withContext(Dispatchers.IO) {
        try {
            val youtubeService = NewPipe.getService(0) // YouTube service
            // Use popular music searches as fallback
            val queries = listOf(
                "popular music 2024",
                "top songs 2024",
                "trending music"
            )

            // Try queries in order until one succeeds
            for (query in queries) {
                try {
                    val searchExtractor = youtubeService.getSearchExtractor(query)
                    searchExtractor.fetchPage()

                    val results = searchExtractor.initialPage.items
                        .filterIsInstance<StreamInfoItem>()
                        .filter { it.streamType != null }
                        .take(20)

                    if (results.isNotEmpty()) {
                        return@withContext Result.success(results)
                    }
                } catch (e: Exception) {
                    // Try next query
                    continue
                }
            }

            // If all queries fail
            Result.failure(Exception("Unable to load trending songs"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
