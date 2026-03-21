package com.example.euphony.data.remote

import android.util.Log
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType

class RelatedSongsFetcher {

    private val TAG = "RelatedSongsFetcher"

    // Increased default limit to 50 to support long listening sessions
    suspend fun getRelatedSongs(videoId: String, limit: Int = 50): Result<List<Song>> {
        return withContext(Dispatchers.IO) {
            try {
                val collectedSongs = mutableListOf<Song>()
                val seenVideoIds = mutableSetOf<String>()
                seenVideoIds.add(videoId) // Don't recommend the song currently playing

                // We start with the original requested video
                var currentSeedVideoId = videoId
                var attempts = 0
                val maxAttempts = 5 // Prevent infinite loops if API is weird

                Log.d(TAG, "Starting Chain Fetch for $limit songs...")

                while (collectedSongs.size < limit && attempts < maxAttempts) {
                    attempts++

                    // 1. Fetch a batch (Youtube Mix or Fallback)
                    val batch = fetchBatch(currentSeedVideoId)

                    if (batch.isEmpty()) {
                        Log.w(TAG, "Batch fetch returned empty. Stopping chain.")
                        break
                    }

                    // 2. Filter duplicates
                    val newSongs = batch.filter { song ->
                        val isNew = song.videoId !in seenVideoIds
                        if (isNew) seenVideoIds.add(song.videoId)
                        isNew
                    }

                    if (newSongs.isEmpty()) {
                        Log.w(TAG, "Batch contained only duplicates. Stopping to prevent loops.")
                        break
                    }

                    // 3. Add to our master list
                    collectedSongs.addAll(newSongs)
                    Log.d(TAG, "Batch #$attempts: Added ${newSongs.size} songs. Total: ${collectedSongs.size}")

                    // 4. PREPARE FOR NEXT BATCH:
                    // Use the LAST song of this batch as the "seed" for the next batch.
                    // This creates the "Radio" effect where the music evolves naturally.
                    currentSeedVideoId = newSongs.last().videoId
                }

                // If we have at least SOME songs, return success
                if (collectedSongs.isNotEmpty()) {
                    // Trim to limit if we went slightly over
                    val finalSanitized = collectedSongs.take(limit)
                    return@withContext Result.success(finalSanitized)
                } else {
                    return@withContext Result.failure(Exception("Could not fetch any related songs"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching related songs: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Fetches a single "page" or "mix" based on a seed video.
     * Tries the RD Mix first, falls back to Tag Search if necessary.
     */
    private suspend fun fetchBatch(seedVideoId: String): List<Song> {
        return try {
            // STRATEGY A: Try the official YouTube Mix (RD List)
            // This is the highest quality source.
            val mixId = "RD$seedVideoId"
            val mixUrl = "https://www.youtube.com/playlist?list=$mixId"

            val mixSongs = fetchFromPlaylist(mixUrl, seedVideoId)

            if (mixSongs.isNotEmpty()) {
                return mixSongs
            }

            // STRATEGY B: Fallback to Smart Search (Tags/Artist)
            // If RD list fails (rare, but happens on new videos), use search.
            return fetchFromSmartSearch(seedVideoId)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching batch for $seedVideoId", e)
            emptyList()
        }
    }

    private fun fetchFromPlaylist(url: String, excludeId: String): List<Song> {
        return try {
            val service = org.schabi.newpipe.extractor.NewPipe.getService(0) // YouTube service
            val playlistExtractor = service.getPlaylistExtractor(url)
            playlistExtractor.fetchPage()

            playlistExtractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .filter { isValidStream(it) }
                .mapNotNull { parseStreamItem(it) }
                .filter { it.videoId != excludeId }
        } catch (e: Exception) {
            Log.w(TAG, "Playlist fetch failed: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchFromSmartSearch(videoId: String): List<Song> {
        try {
            // Need video details to generate a search query
            val service = org.schabi.newpipe.extractor.NewPipe.getService(0) // YouTube service
            val videoUrl = "https://www.youtube.com/watch?v=$videoId"
            val extractor = service.getStreamExtractor(videoUrl)
            extractor.fetchPage()

            val artist = extractor.uploaderName ?: ""
            val title = extractor.name

            // Construct query: "Artist Title Mix"
            val query = "$artist $title Mix"

            val searchExtractor = service.getSearchExtractor(query)
            searchExtractor.fetchPage()

            return searchExtractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .filter { isValidStream(it) }
                .mapNotNull { parseStreamItem(it) }
                .filter { it.videoId != videoId }

        } catch (e: Exception) {
            Log.w(TAG, "Smart search fallback failed", e)
            return emptyList()
        }
    }

    // --- Helpers ---

    private fun isValidStream(item: StreamInfoItem): Boolean {
        return item.streamType == StreamType.VIDEO_STREAM &&
                item.duration > 45 && // Ignore shorts
                item.duration < 900   // Ignore long podcasts
    }

    private fun parseStreamItem(item: StreamInfoItem): Song? {
        return try {
            val extractedVideoId = extractVideoId(item.url)
            if (extractedVideoId.isEmpty()) return null

            Song(
                id = item.url.hashCode().toString(),
                videoId = extractedVideoId,
                title = item.name,
                artist = item.uploaderName ?: "Unknown Artist",
                thumbnailUrl = getThumbnailUrl(item),
                duration = if (item.duration > 0) item.duration * 1000L else 0L,
                album = ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getThumbnailUrl(item: StreamInfoItem): String {
        return try {
            item.thumbnails?.maxByOrNull { it.height }?.url
                ?: item.thumbnails?.firstOrNull()?.url
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractVideoId(url: String): String {
        return try {
            val patterns = listOf(
                "(?:v=|/)([a-zA-Z0-9_-]{11})".toRegex(),
                "youtu\\.be/([a-zA-Z0-9_-]{11})".toRegex(),
                "embed/([a-zA-Z0-9_-]{11})".toRegex()
            )

            for (pattern in patterns) {
                val match = pattern.find(url)
                if (match != null) {
                    return match.groupValues[1]
                }
            }
            ""
        } catch (e: Exception) {
            ""
        }
    }
}