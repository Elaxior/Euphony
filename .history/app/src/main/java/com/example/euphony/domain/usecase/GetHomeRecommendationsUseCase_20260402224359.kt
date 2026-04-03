package com.example.euphony.domain.usecase

import android.util.Log
import com.example.euphony.data.local.dao.HistoryDao
import com.example.euphony.data.remote.TrendingMusicFetcher
import com.example.euphony.domain.model.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gets recommendations for home screen with PARALLEL loading and CACHING:
 * - Recently played from history (REAL-TIME via Flow)
 * - Personalized recommendations based on listening patterns
 * - Trending music from YouTube
 */
class GetHomeRecommendationsUseCase(
    private val historyDao: HistoryDao,
    private val trendingMusicFetcher: TrendingMusicFetcher
) {
    companion object {
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }

    private val mutex = Mutex()

    // Cache for recommended and trending (not recently played - that's real-time)
    private var cachedRecommended: List<Song>? = null
    private var cachedTrending: List<Song>? = null
    private var cacheTimestamp: Long = 0

    /**
     * Get recently played as a Flow for real-time updates
     */
    fun observeRecentlyPlayed(): Flow<List<Song>> {
        return historyDao.getRecentHistory(10).map { historyList ->
            historyList.map { entity ->
                Song(
                    id = entity.videoId,
                    videoId = entity.videoId,
                    title = entity.title,
                    artist = entity.artist,
                    thumbnailUrl = entity.thumbnailUrl,
                    duration = entity.duration
                )
            }
        }
    }

    /**
     * Get recommended and trending (with caching)
     */
    suspend fun getStaticRecommendations(forceRefresh: Boolean = false): StaticRecommendations = mutex.withLock {
        val now = System.currentTimeMillis()
        val isCacheValid = cachedRecommended != null &&
                cachedTrending != null &&
                (now - cacheTimestamp) < CACHE_DURATION_MS

        if (!forceRefresh && isCacheValid) {
            Log.d("GetHomeRecs", "Returning cached static data")
            return@withLock StaticRecommendations(
                recommended = cachedRecommended!!,
                trending = cachedTrending!!
            )
        }

        Log.d("GetHomeRecs", "Fetching fresh static data")
        return@withLock loadFreshStaticRecommendations().also {
            cachedRecommended = it.recommended
            cachedTrending = it.trending
            cacheTimestamp = now
        }
    }

    private suspend fun loadFreshStaticRecommendations(): StaticRecommendations = coroutineScope {
        // Load trending and recommended in parallel
        val trendingDeferred = async { loadTrending() }
        val recommendedDeferred = async { loadRecommendations() }

        val trending = trendingDeferred.await()
        val recommended = recommendedDeferred.await()

        Log.d("GetHomeRecs", "Recommended: ${recommended.size}")
        Log.d("GetHomeRecs", "Trending: ${trending.size}")

        StaticRecommendations(
            recommended = recommended,
            trending = trending
        )
    }

    private suspend fun loadTrending(): List<Song> {
        return trendingMusicFetcher.fetchTrendingMusic(20)
            .getOrElse {
                Log.e("GetHomeRecs", "Error fetching trending", it)
                emptyList()
            }
    }

    private suspend fun loadRecommendations(): List<Song> = coroutineScope {
        try {
            val history = historyDao.getRecentHistoryList()

            if (history.isEmpty()) {
                return@coroutineScope fetchPopularMusic()
            }

            val now = System.currentTimeMillis()
            val historyIds = history.map { it.videoId }.toSet()
            val artistScores = mutableMapOf<String, Double>()
            val keywordScores = mutableMapOf<String, Double>()

            history.take(40).forEachIndexed { index, item ->
                val recencyWeight = 1.0 / (index + 1)
                val ageHours = ((now - item.playedAt).coerceAtLeast(0L) / 3600000.0)
                val freshnessWeight = 1.0 / (1.0 + (ageHours / 24.0))
                val combinedWeight = recencyWeight * 0.7 + freshnessWeight * 0.3

                artistScores[item.artist] = (artistScores[item.artist] ?: 0.0) + combinedWeight

                extractKeywords(item.title).forEach { keyword ->
                    keywordScores[keyword] = (keywordScores[keyword] ?: 0.0) + combinedWeight
                }
            }

            val topArtists = artistScores
                .entries
                .sortedByDescending { it.value }
                .take(4)
                .map { it.key }

            val topKeywords = keywordScores
                .entries
                .sortedByDescending { it.value }
                .take(4)
                .map { it.key }

            val seedQueries = (
                    topArtists.flatMap { artist ->
                        listOf(
                            "$artist official audio",
                            "$artist popular songs"
                        )
                    } +
                            topKeywords.map { keyword -> "$keyword song" } +
                            listOf("fresh music mix", "new top songs")
                    )
                .distinct()
                .take(10)

            val recommendations = mutableListOf<Song>()

            val artistSongs = seedQueries.map { query ->
                async {
                    trendingMusicFetcher.fetchMusicByCategory(query, 6)
                        .getOrElse { emptyList() }
                }
            }.map { it.await() }

            artistSongs.forEach { songs ->
                recommendations.addAll(songs)
            }

            recommendations
                .filter { it.videoId !in historyIds }
                .distinctBy { it.videoId }
                .sortedByDescending { song ->
                    scoreRecommendation(
                        song = song,
                        preferredArtists = topArtists,
                        preferredKeywords = topKeywords.toSet()
                    )
                }
                .take(20)

        } catch (e: Exception) {
            Log.e("GetHomeRecs", "Error generating recommendations", e)
            emptyList()
        }
    }

    private suspend fun fetchPopularMusic(): List<Song> = coroutineScope {
        val categories = listOf("pop hits", "trending music", "top songs")
        val recommendations = mutableListOf<Song>()

        val categorySongs = categories.map { category ->
            async {
                trendingMusicFetcher.fetchMusicByCategory(category, 5)
                    .getOrElse { emptyList() }
            }
        }.map { it.await() }

        categorySongs.forEach { songs ->
            recommendations.addAll(songs)
        }

        recommendations.distinctBy { it.videoId }.take(15)
    }

    /**
     * Clear cache manually (call on refresh)
     */
    fun clearCache() {
        cachedRecommended = null
        cachedTrending = null
        cacheTimestamp = 0
    }

    private fun scoreRecommendation(
        song: Song,
        preferredArtists: List<String>,
        preferredKeywords: Set<String>
    ): Double {
        val normalizedArtist = normalize(song.artist)
        val normalizedTitle = normalize(song.title)

        val artistBoost = preferredArtists
            .mapIndexed { index, artist ->
                val normalizedSeed = normalize(artist)
                val matches = normalizedArtist.contains(normalizedSeed) ||
                        normalizedSeed.contains(normalizedArtist)
                if (matches) 4.0 - index else 0.0
            }
            .maxOrNull() ?: 0.0

        val keywordBoost = preferredKeywords.sumOf { keyword ->
            if (normalizedTitle.contains(keyword)) 0.8 else 0.0
        }

        val durationSeconds = (song.duration / 1000L).toInt()
        val durationBoost = if (durationSeconds in 110..420) 0.4 else 0.0

        return artistBoost + keywordBoost + durationBoost
    }

    private fun extractKeywords(title: String): Set<String> {
        val stopWords = setOf(
            "official", "video", "audio", "lyrics", "song", "feat", "with", "from", "live"
        )

        return normalize(title)
            .split(" ")
            .filter { word -> word.length > 2 && word !in stopWords }
            .toSet()
    }

    private fun normalize(value: String): String {
        return value
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}

data class StaticRecommendations(
    val recommended: List<Song>,
    val trending: List<Song>
)
