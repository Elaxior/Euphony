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

            val topArtists = history
                .groupBy { it.artist }
                .mapValues { it.value.size }
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }
            val recentTitles = history.take(5).map { "${it.artist} ${it.title}" }

            val recommendations = mutableListOf<Song>()

            val artistSongs = topArtists.map { artist ->
                async {
                    trendingMusicFetcher.fetchMusicByCategory("$artist mix", 6)
                        .getOrElse { emptyList() }
                }
            } + recentTitles.map { seed ->
                async {
                    trendingMusicFetcher.fetchMusicByCategory(seed, 4)
                        .getOrElse { emptyList() }
                }
            }.map { it.await() }

            artistSongs.forEach { songs ->
                recommendations.addAll(songs)
            }

            val historyIds = history.map { it.videoId }.toSet()
            recommendations
                .filter { it.videoId !in historyIds }
                .distinctBy { it.videoId }
                .take(20)

        } catch (e: Exception) {
            Log.e("GetHomeRecs", "Error generating recommendations", e)
            emptyList()
        }
    }

    private suspend fun fetchPopularMusic(): List<Song> = coroutineScope {
        val categories = listOf("pop hits", "trending music", "top songs", "viral songs", "new music friday")
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
}

data class StaticRecommendations(
    val recommended: List<Song>,
    val trending: List<Song>
)
