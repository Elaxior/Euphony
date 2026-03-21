package com.example.euphony.data.remote

import android.util.Log
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.util.concurrent.TimeUnit

/**
 * Fetches trending music videos from YouTube
 * Uses search-based approach since kiosk API is unreliable
 */
class TrendingMusicFetcher {

    companion object {
        private const val TAG = "TrendingMusicFetcher"
    }

    /**
     * Fetch trending music using multiple search queries
     */
    suspend fun fetchTrendingMusic(limit: Int = 20): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching trending music via search...")

            val trendingSongs = mutableListOf<Song>()

            // Use multiple trending search terms
            val searchTerms = listOf(
                "trending songs 2025",
                "viral music",
                "top hits 2025"
            )

            for (term in searchTerms) {
                if (trendingSongs.size >= limit) break

                try {
                    val songs = searchMusic(term, (limit - trendingSongs.size))
                    trendingSongs.addAll(songs)
                    Log.d(TAG, "Added ${songs.size} songs from '$term'")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to fetch for term '$term': ${e.message}")
                }
            }

            // Remove duplicates
            val uniqueSongs = trendingSongs.distinctBy { it.videoId }.take(limit)

            Log.d(TAG, "Total trending songs fetched: ${uniqueSongs.size}")

            if (uniqueSongs.isEmpty()) {
                throw Exception("No trending songs found")
            }

            Result.success(uniqueSongs)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching trending music: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch music by category/search term for recommendations
     */
    suspend fun fetchMusicByCategory(
        category: String,
        limit: Int = 10
    ): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching music for category: $category")
            val songs = searchMusic(category, limit)
            Log.d(TAG, "Fetched ${songs.size} songs for category: $category")
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching category music: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Search for music on YouTube
     */
    private suspend fun searchMusic(query: String, limit: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            try {
                val youtubeService = NewPipe.getService(0) // YouTube service
                val searchExtractor = youtubeService.getSearchExtractor(query)
                searchExtractor.fetchPage()

                searchExtractor.initialPage.items
                    .filterIsInstance<StreamInfoItem>()
                    .filter { item ->
                        // Filter for music-like content
                        val duration = item.duration
                        duration in 30..900 // 30 seconds to 15 minutes
                    }
                    .take(limit)
                    .mapNotNull { item ->
                        try {
                            val videoId = extractVideoId(item.url)
                            Song(
                                id = videoId,
                                videoId = videoId,
                                title = item.name ?: "Unknown Title",
                                artist = item.uploaderName ?: "Unknown Artist",
                                thumbnailUrl = item.thumbnails?.firstOrNull()?.url ?: "",
                                duration = item.duration * 1000L
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error converting item: ${e.message}")
                            null
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Search failed for '$query': ${e.message}")
                emptyList()
            }
        }
    }

    /**
     * Extract video ID from various URL formats
     */
    private fun extractVideoId(url: String): String {
        return when {
            url.contains("v=") -> url.substringAfter("v=").substringBefore("&")
            url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
            url.contains("/watch/") -> url.substringAfter("/watch/").substringBefore("?")
            else -> url.takeLast(11) // YouTube video IDs are 11 characters
        }
    }

    /**
     * Simple downloader implementation for NewPipeExtractor
     */
    private class DownloaderImpl private constructor() : Downloader() {

        companion object {
            val instance: DownloaderImpl by lazy { DownloaderImpl() }
        }

        private val client = okhttp3.OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        override fun execute(request: Request): Response {
            val httpMethod = request.httpMethod()

            // Create request body for POST requests
            val requestBody: RequestBody? = when {
                httpMethod == "POST" -> {
                    val body = request.dataToSend()
                    if (body != null && body.isNotEmpty()) {
                        body.toRequestBody("application/json; charset=utf-8".toMediaType())
                    } else {
                        // Empty body for POST requests without data
                        ByteArray(0).toRequestBody(null)
                    }
                }
                else -> null
            }

            val requestBuilder = okhttp3.Request.Builder()
                .method(httpMethod, requestBody)
                .url(request.url())

            // Add headers
            request.headers()?.forEach { (key, values) ->
                values.forEach { value ->
                    requestBuilder.addHeader(key, value)
                }
            }

            val response = client.newCall(requestBuilder.build()).execute()

            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                response.body?.string(),
                response.request.url.toString()
            )
        }
    }
}
