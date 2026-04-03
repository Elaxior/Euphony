package com.example.euphony.data.remote

import android.util.Log
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LyricsFetcher {

    companion object {
        private const val TAG = "LyricsFetcher"
        private const val BASE_URL = "https://api.lyrics.ovh/v1"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    suspend fun fetchLyrics(song: Song): Result<String> = withContext(Dispatchers.IO) {
        val artist = sanitize(song.artist)
        val title = sanitize(song.title)

        if (artist.isBlank() || title.isBlank()) {
            return@withContext Result.failure(Exception("Lyrics not available"))
        }

        return@withContext try {
            val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
            val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
            val request = Request.Builder()
                .url("$BASE_URL/$encodedArtist/$encodedTitle")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful || body.isBlank()) {
                return@withContext Result.failure(Exception("Lyrics not found"))
            }

            val json = JSONObject(body)
            val lyrics = json.optString("lyrics").trim()

            if (lyrics.isBlank()) {
                Result.failure(Exception("Lyrics not found"))
            } else {
                Result.success(lyrics)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Lyrics fetch failed for ${song.title}", e)
            Result.failure(e)
        }
    }

    private fun sanitize(value: String): String {
        return value
            .replace(Regex("\\(.*?\\)|\\[.*?\\]"), "")
            .replace(Regex("(?i)official|video|audio|lyrics|hd|4k"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
