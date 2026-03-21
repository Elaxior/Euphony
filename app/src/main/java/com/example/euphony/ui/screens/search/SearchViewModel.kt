package com.example.euphony.ui.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.euphony.domain.model.Song
import com.example.euphony.domain.usecase.AddSearchToHistoryUseCase
import com.example.euphony.domain.usecase.ClearSearchHistoryUseCase
import com.example.euphony.domain.usecase.DeleteSearchHistoryItemUseCase
import com.example.euphony.domain.usecase.GetRecentSearchesUseCase
import com.example.euphony.domain.usecase.SearchSongsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val searchResults: List<Song> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSearchActive: Boolean = false
)

class SearchViewModel(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val addSearchToHistoryUseCase: AddSearchToHistoryUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val deleteSearchHistoryItemUseCase: DeleteSearchHistoryItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    companion object {
        private const val TAG = "SearchViewModel"
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val MIN_LOADING_TIME = 200L
    }

    init {
        loadRecentSearches()
        observeQueryChanges()
    }

    /**
     * Load recent searches from database
     */
    private fun loadRecentSearches() {
        viewModelScope.launch {
            getRecentSearchesUseCase()
                .collect { searches ->
                    _uiState.update { it.copy(recentSearches = searches) }
                }
        }
    }

    /**
     * Observe query changes with debounce (auto-search without saving to history)
     */
    @OptIn(FlowPreview::class)
    private fun observeQueryChanges() {
        viewModelScope.launch {
            _queryFlow
                .debounce(DEBOUNCE_TIMEOUT)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank() && query.length >= 2) {
                        performSearch(query, saveToHistory = false)
                    }
                }
        }
    }

    /**
     * Update search query
     */
    fun onQueryChange(query: String) {
        _uiState.update {
            it.copy(
                query = query,
                error = null,
                isSearchActive = query.isNotBlank()
            )
        }

        // Cancel previous search
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    searchResults = emptyList(),
                    isLoading = false,
                    error = null
                )
            }
            return
        }

        // Update query flow for debouncing
        _queryFlow.value = query
    }

    /**
     * Explicit search (when user presses Enter/Search button)
     * This WILL save to history
     */
    fun onSearch() {
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank() && currentQuery.length >= 2) {
            performSearch(currentQuery, saveToHistory = true)
        }
    }

    /**
     * Perform search
     * @param saveToHistory - if true, saves to search history after successful search
     */
    private fun performSearch(query: String, saveToHistory: Boolean) {
        // Don't search if query is too short
        if (query.length < 2) return

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            // Set loading state
            _uiState.update { it.copy(isLoading = true, error = null) }

            Log.d(TAG, "Searching for: $query (saveToHistory: $saveToHistory)")

            try {
                // Perform search
                val result = searchSongsUseCase(query)

                // Calculate remaining time for minimum loading display
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = MIN_LOADING_TIME - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                result.onSuccess { songs ->
                    _uiState.update {
                        it.copy(
                            searchResults = songs,
                            isLoading = false,
                            error = null
                        )
                    }

                    // Only save to history if explicitly requested AND got results
                    if (saveToHistory && songs.isNotEmpty()) {
                        addSearchToHistoryUseCase(query)
                        Log.d(TAG, "Saved to history: $query")
                    }

                    Log.d(TAG, "Found ${songs.size} results")
                }.onFailure { exception ->
                    // Only show error if it's not a cancellation
                    if (exception !is CancellationException) {
                        Log.e(TAG, "Search error: ${exception.message}", exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "Search failed",
                                searchResults = emptyList()
                            )
                        }
                    } else {
                        // Just hide loading on cancellation
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }

            } catch (e: Exception) {
                // Calculate remaining time for minimum loading display
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = MIN_LOADING_TIME - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                // Only show error if it's not a cancellation
                if (e !is CancellationException) {
                    Log.e(TAG, "Search error: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Search failed",
                            searchResults = emptyList()
                        )
                    }
                } else {
                    // Just hide loading on cancellation
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    /**
     * Search from history - saves to history to update timestamp
     */
    fun onHistoryItemClick(query: String) {
        onQueryChange(query)
        // Perform search immediately and save to history
        performSearch(query, saveToHistory = true)
    }

    /**
     * Delete history item
     */
    fun onDeleteHistoryItem(query: String) {
        viewModelScope.launch {
            deleteSearchHistoryItemUseCase(query)
        }
    }

    /**
     * Clear all search history
     */
    fun onClearHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
        }
    }

    /**
     * Clear search and results
     */
    fun onClearSearch() {
        searchJob?.cancel()
        _uiState.update {
            SearchUiState(recentSearches = it.recentSearches)
        }
        _queryFlow.value = ""
    }

    /**
     * Retry search
     */
    fun onRetry() {
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery, saveToHistory = true)
        }
    }
}
