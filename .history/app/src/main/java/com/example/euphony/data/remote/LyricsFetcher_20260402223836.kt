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
import kotlin.math.max

class LyricsFetcher {

    companion object {
        private const val TAG = "LyricsFetcher"
        private const val BASE_URL = "https://lrclib.net/api"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    suspend fun fetchLyrics(song: Song): Result<String> = withContext(Dispatchers.IO) {
        val artistCandidates = buildArtistCandidates(song.artist)
        val titleCandidates = buildTitleCandidates(song.title)

        if (artistCandidates.isEmpty() || titleCandidates.isEmpty()) {
            return@withContext Result.failure(Exception("Lyrics not available"))
        }

        return@withContext try {
            // 1) Exact endpoint with strongest candidate pairs first
            for (artist in artistCandidates.take(3)) {
                for (title in titleCandidates.take(3)) {
                    val exactLyrics = fetchExactLyrics(artist, title)
                    if (!exactLyrics.isNullOrBlank()) {
                        return@withContext Result.success(exactLyrics)
                    }
                }
            }

            // 2) Structured search endpoint
            for (artist in artistCandidates.take(3)) {
                for (title in titleCandidates.take(3)) {
                    val fallbackLyrics = fetchSearchLyrics(artist, title)
                    if (!fallbackLyrics.isNullOrBlank()) {
                        return@withContext Result.success(fallbackLyrics)
                    }
                }
            }

            // 3) Query-based fallback search for difficult metadata
            val queryCandidates = mutableListOf<String>()
            queryCandidates += artistCandidates.flatMap { artist ->
                titleCandidates.take(3).map { title -> "$artist $title" }
            }
            queryCandidates += titleCandidates

            for (query in queryCandidates.distinct().take(8)) {
                val queryLyrics = fetchSearchLyricsByQuery(query)
                if (!queryLyrics.isNullOrBlank()) {
                    return@withContext Result.success(queryLyrics)
                }
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

        return parseBestLyricsFromResults(
            results = JSONArray(body),
            artistHint = artist,
            titleHint = title
        )
    }

    private fun fetchSearchLyricsByQuery(query: String): String? {
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val request = Request.Builder()
            .url("$BASE_URL/search?q=$encodedQuery")
            .get()
            .header("Accept", "application/json")
            .header("User-Agent", "Euphony/1.0")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful || body.isBlank()) {
            return null
        }

        return parseBestLyricsFromResults(
            results = JSONArray(body),
            artistHint = "",
            titleHint = query
        )
    }

    private fun parseBestLyricsFromResults(
        results: JSONArray,
        artistHint: String,
        titleHint: String
    ): String? {
        var bestLyrics: String? = null
        var bestScore = Int.MIN_VALUE

        val normalizedArtistHint = sanitize(artistHint).lowercase()
        val normalizedTitleHint = sanitize(titleHint).lowercase()

        for (index in 0 until results.length()) {
            val json = results.optJSONObject(index) ?: continue
            val lyrics = parseLyricsFromJsonObject(json) ?: continue

            val candidateArtist = sanitize(json.optString("artistName")).lowercase()
            val candidateTitle = sanitize(json.optString("trackName")).lowercase()

            val score = computeMatchScore(
                artistHint = normalizedArtistHint,
                titleHint = normalizedTitleHint,
                candidateArtist = candidateArtist,
                candidateTitle = candidateTitle
            )

            if (score > bestScore) {
                bestScore = score
                bestLyrics = lyrics
            }
        }

        return bestLyrics
    }

    private fun computeMatchScore(
        artistHint: String,
        titleHint: String,
        candidateArtist: String,
        candidateTitle: String
    ): Int {
        var score = 0

        if (artistHint.isNotBlank()) {
            if (candidateArtist == artistHint) score += 80
            if (candidateArtist.contains(artistHint) || artistHint.contains(candidateArtist)) score += 30
        }

        if (titleHint.isNotBlank()) {
            if (candidateTitle == titleHint) score += 80
            if (candidateTitle.contains(titleHint) || titleHint.contains(candidateTitle)) score += 40

            val sharedTokenCount = tokenize(candidateTitle).intersect(tokenize(titleHint)).size
            score += sharedTokenCount * 6
        }

        return score
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
            .replace(Regex("(?i)official|video|audio|lyrics|hd|4k|feat\\.?|ft\\.?|topic"), "")
            .replace(Regex("(?i)\\b(remix|version|live|karaoke)\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun buildArtistCandidates(rawArtist: String): List<String> {
        val sanitized = sanitize(rawArtist)
        if (sanitized.isBlank()) return emptyList()

        val primaryArtist = sanitized
            .split(",", "&", " x ", " and ", ";")
            .firstOrNull()
            ?.trim()
            .orEmpty()

        return listOf(sanitized, primaryArtist)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun buildTitleCandidates(rawTitle: String): List<String> {
        val sanitized = sanitize(rawTitle)
        if (sanitized.isBlank()) return emptyList()

        val withoutDashSuffix = sanitized.substringBefore(" - ").trim()
        val withoutPipeSuffix = sanitized.substringBefore("|").trim()

        return listOf(sanitized, withoutDashSuffix, withoutPipeSuffix)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun tokenize(value: String): Set<String> {
        return value
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split(" ")
            .filter { it.length >= 3 }
            .toSet()
    }
}
