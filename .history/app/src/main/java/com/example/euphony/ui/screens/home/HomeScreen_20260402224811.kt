package com.example.euphony.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.euphony.di.AppContainer
import com.example.euphony.ui.components.GradientBackground
import com.example.euphony.ui.components.SectionHeader
import com.example.euphony.ui.components.SongCard
import com.example.euphony.ui.theme.EuphonyThemePreset
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    currentThemePreset: EuphonyThemePreset,
    onThemeSelected: (EuphonyThemePreset) -> Unit,
    viewModel: HomeViewModel = viewModel(
        factory = AppContainer.provideHomeViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    GradientBackground {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Always show greeting immediately
                item(contentType = "greeting") {
                    GreetingHeader(
                        currentThemePreset = currentThemePreset,
                        onThemeSelected = onThemeSelected
                    )
                }

                // Show loading indicator if first load
                if (uiState.isInitialLoading &&
                    uiState.recentlyPlayed.isEmpty() &&
                    uiState.recommended.isEmpty() &&
                    uiState.trending.isEmpty()) {
                    item(contentType = "loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "Loading your music...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFB3B3B3)
                                )
                            }
                        }
                    }
                }

                // Show error if needed
                if (uiState.error != null &&
                    uiState.recentlyPlayed.isEmpty() &&
                    uiState.recommended.isEmpty() &&
                    uiState.trending.isEmpty()) {
                    item(contentType = "error") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "😕",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Text(
                                    text = "Couldn't load content",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = uiState.error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB3B3B3),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                }

                // Recently Played Section (REAL-TIME)
                if (uiState.recentlyPlayed.isNotEmpty()) {
                    item(contentType = "section_header") {
                        SectionHeader(title = "Recently Played")
                    }

                    item(contentType = "horizontal_list") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.recentlyPlayed,
                                key = { song -> "recent_${song.id}" }
                            ) { song ->
                                SongCard(
                                    song = song,
                                    onClick = {
                                        viewModel.playSong(song)
                                        onNavigateToPlayer()
                                    }
                                )
                            }
                        }
                    }
                }

                // Recommended For You Section
                if (uiState.recommended.isNotEmpty()) {
                    item(contentType = "section_header") {
                        SectionHeader(title = "Recommended For You")
                    }

                    item(contentType = "horizontal_list") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.recommended,
                                key = { song -> "recommended_${song.id}" }
                            ) { song ->
                                SongCard(
                                    song = song,
                                    onClick = {
                                        viewModel.playSong(song)
                                        onNavigateToPlayer()
                                    }
                                )
                            }
                        }
                    }
                }

                // Trending Mix Section
                if (uiState.trending.isNotEmpty()) {
                    item(contentType = "section_header") {
                        SectionHeader(title = "Trending Mix")
                    }

                    item(contentType = "horizontal_list") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.trending,
                                key = { song -> "trending_${song.id}" }
                            ) { song ->
                                SongCard(
                                    song = song,
                                    onClick = {
                                        viewModel.playSong(song)
                                        onNavigateToPlayer()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingHeader() {
private fun GreetingHeader(
    currentThemePreset: EuphonyThemePreset,
    onThemeSelected: (EuphonyThemePreset) -> Unit
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    var showThemeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = (-1).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Theme: ${currentThemePreset.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }

        Box {
            IconButton(onClick = { showThemeMenu = true }) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Change theme",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = showThemeMenu,
                onDismissRequest = { showThemeMenu = false }
            ) {
                EuphonyThemePreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.displayName) },
                        onClick = {
                            onThemeSelected(preset)
                            showThemeMenu = false
                        },
                        leadingIcon = {
                            if (preset == currentThemePreset) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    }
}
