package com.example.euphony

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.euphony.di.AppContainer
import com.example.euphony.service.PlaybackService
import com.example.euphony.ui.components.BottomNavigationBar
import com.example.euphony.ui.components.MiniPlayer
import com.example.euphony.ui.navigation.NavGraph
import com.example.euphony.ui.theme.EuphonyThemePreset
import com.example.euphony.ui.theme.EuphonyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AppContainer
        AppContainer.initialize(applicationContext)

        // Start PlaybackService
        startPlaybackService()

        setContent {
            var selectedThemeIndex by rememberSaveable { mutableIntStateOf(0) }
            val selectedTheme = EuphonyThemePreset.fromIndex(selectedThemeIndex)

            EuphonyTheme(themePreset = selectedTheme) {
                val navController = rememberNavController()
                val queueRepository = remember { AppContainer.provideFakeQueueRepository() }
                val queueState by queueRepository.getQueueState().collectAsState(initial = com.example.euphony.domain.model.QueueState())
                val playerRepository = remember { AppContainer.providePlayerRepository() }
                val currentPosition by playerRepository.currentPosition.collectAsState()
                val duration by playerRepository.duration.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {
                            // Mini Player above Bottom Navigation
                            if (queueState.currentSong != null) {
                                MiniPlayer(
                                    song = queueState.currentSong,
                                    isPlaying = queueState.isPlaying,
                                    currentPosition = currentPosition,
                                    duration = duration,
                                    onPlayPauseClick = {
                                        if (queueState.isPlaying) {
                                            playerRepository.pause()
                                        } else {
                                            playerRepository.resume()
                                        }
                                    },
                                    onMiniPlayerClick = {
                                        navController.navigate("player") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Bottom Navigation Bar
                            BottomNavigationBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        currentThemePreset = selectedTheme,
                        onThemeSelected = { preset -> selectedThemeIndex = preset.ordinal },
                        onNavigateToPlayer = {
                            navController.navigate("player") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }

    private fun startPlaybackService() {
        val intent = Intent(this, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Service will continue in background if music is playing
    }
}
