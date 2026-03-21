package com.example.euphony.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.stream.StreamInfo

class AudioStreamExtractor {

    companion object {
        private const val TAG = "AudioStreamExtractor"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1500L
    }

    /**
     * Extract best audio stream URL for a video ID with retry logic
     * Uses dev-SNAPSHOT API for latest YouTube compatibility
     */
    suspend fun getAudioStreamUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        // Try multiple times with exponential backoff
        repeat(MAX_RETRIES) { attempt ->
            try {
                Log.d(TAG, "🎵 Extracting audio for: $videoId (Attempt ${attempt + 1}/$MAX_RETRIES)")

                // Build full YouTube URL
                val videoUrl = "https://www.youtube.com/watch?v=$videoId"

                // Get stream info using dev-SNAPSHOT API
                val streamInfo = StreamInfo.getInfo(
                    NewPipe.getService(0), // YouTube service ID
                    videoUrl
                )

                Log.d(TAG, "📊 Audio streams found: ${streamInfo.audioStreams.size}")

                if (streamInfo.audioStreams.isEmpty()) {
                    throw Exception("No audio streams available")
                }

                // Strategy 1: Try to get highest bitrate audio-only stream
                val audioStream = streamInfo.audioStreams
                    .filter { it.content?.isNotBlank() == true }
                    .maxByOrNull { it.averageBitrate }
                    ?: streamInfo.audioStreams.firstOrNull()
                    ?: throw Exception("No valid audio stream found")

                val streamUrl = audioStream.content

                if (streamUrl.isNullOrBlank()) {
                    throw Exception("Stream URL is empty")
                }

                Log.d(TAG, "✅ SUCCESS! Bitrate: ${audioStream.averageBitrate} kbps")

                return@withContext Result.success(streamUrl)

            } catch (e: org.schabi.newpipe.extractor.exceptions.ExtractionException) {
                Log.w(TAG, "⚠️ Extraction failed (${attempt + 1}/$MAX_RETRIES): ${e.message}")
                lastException = Exception("YouTube temporarily unavailable. Please retry.")

                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error (${attempt + 1}/$MAX_RETRIES): ${e.message}")
                lastException = e

                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }

        // All retries failed
        Log.e(TAG, "❌ Failed after $MAX_RETRIES attempts: $videoId")
        Result.failure(
            Exception("Cannot load song. ${lastException?.message ?: "Try another one or retry."}")
        )
    }

    /**
     * Get thumbnail URL for video
     */
    fun getThumbnailUrl(videoId: String): String {
        return "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
    }
}
