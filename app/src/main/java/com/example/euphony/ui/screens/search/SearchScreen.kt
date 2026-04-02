package com.example.euphony.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.euphony.domain.model.Song
import com.example.euphony.ui.components.SearchEmptyState
import com.example.euphony.ui.components.SearchErrorState
import com.example.euphony.ui.components.SearchHistoryItem
import com.example.euphony.ui.components.SearchLoadingSkeleton
import com.example.euphony.ui.components.SearchResultCard

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onSongClick: (Song) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0A2E),
                        Color(0xFF0F0520)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.query,
                onQueryChange = { viewModel.onQueryChange(it) },
                onClear = { viewModel.onClearSearch() },
                onSearch = {
                    viewModel.onSearch()  // ✅ EXPLICIT SEARCH - SAVES TO HISTORY
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Content based on state
            when {
                uiState.isLoading -> {
                    SearchLoadingSkeleton()
                }

                uiState.error != null -> {
                    SearchErrorState(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.onRetry() }
                    )
                }

                !uiState.isSearchActive && uiState.recentSearches.isNotEmpty() -> {
                    // Show recent searches when not searching
                    RecentSearchesSection(
                        searches = uiState.recentSearches,
                        onSearchClick = { viewModel.onHistoryItemClick(it) },
                        onDeleteClick = { viewModel.onDeleteHistoryItem(it) },
                        onClearAll = { viewModel.onClearHistory() }
                    )
                }

                uiState.searchResults.isEmpty() && uiState.isSearchActive -> {
                    SearchEmptyState(query = uiState.query)
                }

                uiState.searchResults.isNotEmpty() -> {
                    // Show search results
                    SearchResults(
                        results = uiState.searchResults,
                        onAddToQueue = { song -> viewModel.addToQueue(song) },
                        onSongClick = onSongClick
                    )
                }

                else -> {
                    // Initial empty state
                    InitialEmptyState()
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search songs, artists...",
                color = Color(0xFF808080)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFFB3B3B3)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFFB3B3B3)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1C1C1E),
            unfocusedContainerColor = Color(0xFF1C1C1E),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF9C27B0),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }  // ✅ Triggers explicit search
        )
    )
}

@Composable
private fun RecentSearchesSection(
    searches: List<String>,
    onSearchClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                ),
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            TextButton(
                onClick = onClearAll,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = "Clear All",
                    color = Color(0xFF9C27B0),
                    fontSize = 14.sp
                )
            }
        }

        HorizontalDivider(
            color = Color(0xFF2C2C2E),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // History list
        LazyColumn {
            items(searches) { query ->
                SearchHistoryItem(
                    query = query,
                    onClick = { onSearchClick(query) },
                    onDeleteClick = { onDeleteClick(query) }
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<Song>,
    onAddToQueue: (Song) -> Unit,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(results) { song ->
            SearchResultCard(
                song = song,
                onClick = { onSongClick(song) },
                onMoreClick = { onAddToQueue(song) },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun InitialEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF808080),
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Search for music",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Find your favorite songs and artists",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp
            ),
            color = Color(0xFFB3B3B3)
        )
    }
}
