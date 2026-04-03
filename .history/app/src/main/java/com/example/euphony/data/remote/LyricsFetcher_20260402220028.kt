package com.example.euphony.data.remote

import android.util.Log
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LyricsFetcher {

    companion object {
        private const val TAG = "LyricsFetcher"
        private const val BASE_URL = "https://lrclib.net/api"
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
            val exactLyrics = fetchExactLyrics(artist, title)
            if (!exactLyrics.isNullOrBlank()) {
                return@withContext Result.success(exactLyrics)
            }

            val fallbackLyrics = fetchSearchLyrics(artist, title)
            if (!fallbackLyrics.isNullOrBlank()) {
                return@withContext Result.success(fallbackLyrics)
            }

            Result.failure(Exception("Lyrics not found"))
        } catch (e: Exception) {
            Log.w(TAG, "Lyrics fetch failed for ${song.title}", e)
            Result.failure(e)
        }
    }

    private fun fetchExactLyrics(artist: String, title: String): String? {
        val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val request = Request.Builder()
            .url("$BASE_URL/get?artist_name=$encodedArtist&track_name=$encodedTitle")
            .get()
            .header("Accept", "application/json")
            .header("User-Agent", "Euphony/1.0")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful || body.isBlank()) {
            return null
        }

        return parseLyricsFromJsonObject(JSONObject(body))
    }

    private fun fetchSearchLyrics(artist: String, title: String): String? {
        val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val request = Request.Builder()
            .url("$BASE_URL/search?artist_name=$encodedArtist&track_name=$encodedTitle")
            .get()
            .header("Accept", "application/json")
            .header("User-Agent", "Euphony/1.0")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful || body.isBlank()) {
            return null
        }

        val results = JSONArray(body)
        for (index in 0 until results.length()) {
            val lyrics = parseLyricsFromJsonObject(results.optJSONObject(index) ?: continue)
            if (!lyrics.isNullOrBlank()) {
                return lyrics
            }
        }

        return null
    }

    private fun parseLyricsFromJsonObject(json: JSONObject): String? {
        val plainLyrics = json.optString("plainLyrics").trim()
        if (plainLyrics.isNotBlank()) {
            return plainLyrics
        }

        val syncedLyrics = json.optString("syncedLyrics").trim()
        if (syncedLyrics.isNotBlank()) {
            return syncedLyrics
                .lineSequence()
                .map { line -> line.replace(Regex("^\\[[0-9:.]+]"), "").trim() }
                .filter { it.isNotBlank() }
                .joinToString("\n")
                .trim()
                .ifBlank { null }
        }

        return null
    }

    private fun sanitize(value: String): String {
        return value
            .replace(Regex("\\(.*?\\)|\\[.*?\\]"), "")
            .replace(Regex("(?i)official|video|audio|lyrics|hd|4k|feat\\.?|ft\\.?"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
