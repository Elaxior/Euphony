package com.example.euphony.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.usecase.GetHomeRecommendationsUseCase
import com.example.euphony.domain.usecase.PlaySongUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeRecommendationsUseCase: GetHomeRecommendationsUseCase,
    private val playSongUseCase: PlaySongUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeRecentlyPlayed() // Real-time updates
        loadStaticRecommendations() // Cached data
    }

    /**
     * Observe recently played in real-time
     */
    private fun observeRecentlyPlayed() {
        viewModelScope.launch {
            getHomeRecommendationsUseCase.observeRecentlyPlayed().collect { songs ->
                Log.d(TAG, "Recently Played updated: ${songs.size}")
                _uiState.update { it.copy(recentlyPlayed = songs) }
            }
        }
    }

    /**
     * Load recommended and trending (with cache)
     */
    private fun loadStaticRecommendations(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitialLoading = true, error = null) }

            try {
                val recommendations = getHomeRecommendationsUseCase.getStaticRecommendations(forceRefresh)

                Log.d(TAG, "Recommended: ${recommendations.recommended.size}")
                Log.d(TAG, "Trending: ${recommendations.trending.size}")

                _uiState.update {
                    it.copy(
                        recommended = recommendations.recommended,
                        trending = recommendations.trending,
                        isInitialLoading = false,
                        isRefreshing = false
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading recommendations", e)
                _uiState.update {
                    it.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load recommendations"
                    )
                }
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            try {
                playSongUseCase(song)
                // Recently played will auto-update via Flow!
            } catch (e: Exception) {
                Log.e(TAG, "Error playing song", e)
            }
        }
    }

    /**
     * Pull-to-refresh action
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        getHomeRecommendationsUseCase.clearCache()
        loadStaticRecommendations(forceRefresh = true)
    }
}

data class HomeUiState(
    val recentlyPlayed: List<Song> = emptyList(),
    val recommended: List<Song> = emptyList(),
    val trending: List<Song> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)
