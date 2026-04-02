package com.example.euphony.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.euphony.di.AppContainer
import com.example.euphony.domain.model.Playlist
import com.example.euphony.domain.model.RepeatMode
import com.example.euphony.domain.model.Song
import kotlin.math.roundToInt

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = remember {
        PlayerViewModel(
            getQueueStateUseCase = AppContainer.provideGetQueueStateUseCase(),
            pauseSongUseCase = AppContainer.providePauseSongUseCase(),
            resumeSongUseCase = AppContainer.provideResumeSongUseCase(),
            playNextSongUseCase = AppContainer.providePlayNextSongUseCase(),
            playPreviousSongUseCase = AppContainer.providePlayPreviousSongUseCase(),
            seekToPositionUseCase = AppContainer.provideSeekToPositionUseCase(),
            toggleShuffleUseCase = AppContainer.provideToggleShuffleUseCase(),
            cycleRepeatModeUseCase = AppContainer.provideCycleRepeatModeUseCase(),
            queueRepository = AppContainer.provideFakeQueueRepository(),
            // NEW: Library functionality
            libraryRepository = AppContainer.provideLibraryRepository(),
            createPlaylistUseCase = AppContainer.provideCreatePlaylistUseCase(),
            addSongToPlaylistUseCase = AppContainer.provideAddSongToPlaylistUseCase(),
            // NEW: Download functionality
            downloadManager = AppContainer.provideDownloadManager()
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // FIX: Capture currentSong in a local variable
    val currentSong = uiState.queueState.currentSong

    // NEW: Dialogs
    if (uiState.showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onDismiss = { viewModel.hideAddToPlaylistDialog() },
            onPlaylistSelected = { playlistId ->
                viewModel.addCurrentSongToPlaylist(playlistId)
            },
            onCreateNew = {
                viewModel.hideAddToPlaylistDialog()
                viewModel.showCreatePlaylistDialog()
            }
        )
    }

    if (uiState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { viewModel.hideCreatePlaylistDialog() },
            onConfirm = { name ->
                viewModel.createPlaylistAndAddSong(name)
            }
        )
    }

    // FIX: Use the captured local variable
    if (currentSong != null) {
        FullscreenPlayerWithQueue(
            song = currentSong, // Now using local variable
            queue = uiState.queueState.queue,
            currentIndex = uiState.queueState.currentIndex,
            isPlaying = uiState.queueState.isPlaying,
            isLoading = uiState.queueState.isLoading,
            error = uiState.queueState.error, // Pass error
            canPlayNext = uiState.queueState.hasNext,
            canPlayPrevious = uiState.queueState.hasPrevious,
            isShuffleEnabled = uiState.queueState.playbackMode.isShuffleEnabled,
            repeatMode = uiState.queueState.playbackMode.repeatMode,
            isFavorite = uiState.isFavorite,
            isDownloaded = uiState.isDownloaded, // NEW
            downloadProgress = uiState.downloadProgress, // NEW
            onPlayPauseClick = { viewModel.togglePlayPause() },
            onNextClick = { viewModel.playNext() },
            onPreviousClick = { viewModel.playPrevious() },
            onShuffleClick = { viewModel.toggleShuffle() },
            onRepeatClick = { viewModel.cycleRepeatMode() },
            onSeek = { position -> viewModel.seekTo(position) },
            onQueueItemClick = { index -> viewModel.playQueueSong(index) },
            onNavigateBack = onNavigateBack,
            onFavoriteClick = { viewModel.toggleFavorite() },
            onDownloadClick = { viewModel.downloadSong() }, // NEW
            onAddToPlaylistClick = { viewModel.showAddToPlaylistDialog() },
            onRetryClick = { viewModel.retrySong() }, // Add retry
            onSwipeNext = { viewModel.playNext() },
            onSwipePrevious = { viewModel.playPrevious() }
        )
    } else {
        EmptyPlayerState(onNavigateBack)
    }
}


@Composable
private fun FullscreenPlayerWithQueue(
    song: Song,
    queue: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    error: String?, // Add error parameter
    canPlayNext: Boolean,
    canPlayPrevious: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean, // NEW
    isDownloaded: Boolean, // NEW: Download status
    downloadProgress: Int?, // NEW: Download progress
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onQueueItemClick: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onFavoriteClick: () -> Unit, // NEW
    onDownloadClick: () -> Unit, // NEW: Download action
    onAddToPlaylistClick: () -> Unit, // NEW
    onRetryClick: () -> Unit, // Add retry
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit
) {
    var showQueue by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main player
        FullscreenPlayer(
            song = song,
            isPlaying = isPlaying,
            isLoading = isLoading,
            error = error, // Pass error
            canPlayNext = canPlayNext,
            canPlayPrevious = canPlayPrevious,
            isShuffleEnabled = isShuffleEnabled,
            repeatMode = repeatMode,
            isFavorite = isFavorite, // NEW
            isDownloaded = isDownloaded, // NEW
            downloadProgress = downloadProgress, // NEW
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onShuffleClick = onShuffleClick,
            onRepeatClick = onRepeatClick,
            onSeek = onSeek,
            onNavigateBack = onNavigateBack,
            onQueueClick = { showQueue = !showQueue },
            onFavoriteClick = onFavoriteClick, // NEW
            onDownloadClick = onDownloadClick, // NEW
            onAddToPlaylistClick = onAddToPlaylistClick, // NEW
            onRetryClick = onRetryClick, // Pass retry
            onSwipeNext = onSwipeNext,
            onSwipePrevious = onSwipePrevious
        )

        // Queue bottom sheet
        if (showQueue) {
            QueueBottomSheet(
                queue = queue,
                currentIndex = currentIndex,
                onQueueItemClick = onQueueItemClick,
                onDismiss = { showQueue = false }
            )
        }
    }
}

@Composable
private fun FullscreenPlayer(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean,
    error: String?, // Add error parameter
    canPlayNext: Boolean,
    canPlayPrevious: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean, // NEW
    isDownloaded: Boolean, // NEW: Download status
    downloadProgress: Int?, // NEW: Download progress (0-100 or null)
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onQueueClick: () -> Unit,
    onFavoriteClick: () -> Unit, // NEW
    onDownloadClick: () -> Unit, // NEW: Download action
    onAddToPlaylistClick: () -> Unit, // NEW
    onRetryClick: () -> Unit, // Add retry
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit
) {
    val playerRepository = remember { AppContainer.providePlayerRepository() }
    val currentPosition by playerRepository.currentPosition.collectAsState()
    val duration by playerRepository.duration.collectAsState()

    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var showLyricsPanel by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition) {
        if (!isUserSeeking && duration > 0) {
            sliderPosition = currentPosition.toFloat()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen blurred background
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )

                IconButton(onClick = onQueueClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large album art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(song.videoId) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                when {
                                    totalDrag <= -120f && canPlayNext -> onSwipeNext()
                                    totalDrag >= 120f && canPlayPrevious -> onSwipePrevious()
                                }
                                totalDrag = 0f
                            }
                        ) { _, dragAmount ->
                            totalDrag += dragAmount
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Song info with favorite/playlist actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = Color(0xFFB3B3B3),
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // NEW: Library Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorite Button
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color(0xFFE91E63) else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Download Button
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (downloadProgress != null) {
                            // Show circular progress while downloading
                            CircularProgressIndicator(
                                progress = downloadProgress / 100f,
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "$downloadProgress%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 8.sp
                            )
                        } else {
                            // Show download icon
                            IconButton(onClick = onDownloadClick) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                    contentDescription = if (isDownloaded) "Downloaded" else "Download",
                                    tint = if (isDownloaded) MaterialTheme.colorScheme.primary else Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // Add to Playlist Button
                    IconButton(onClick = onAddToPlaylistClick) {
                        Icon(
                            imageVector = Icons.Default.PlaylistAdd,
                            contentDescription = "Add to playlist",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Lyrics panel toggle
                    IconButton(onClick = { showLyricsPanel = !showLyricsPanel }) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Lyrics",
                            tint = if (showLyricsPanel) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seek bar
            if (duration > 0) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderPosition,
                        onValueChange = { newPosition ->
                            isUserSeeking = true
                            sliderPosition = newPosition
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            onSeek(sliderPosition.toLong())
                        },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0xFF535353)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(if (isUserSeeking) sliderPosition.toLong() else currentPosition),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFFB3B3B3)
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFFB3B3B3)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isShuffleEnabled) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else Color(0xFF808080),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onPreviousClick,
                    enabled = canPlayPrevious && !isLoading,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(40.dp),
                        tint = if (canPlayPrevious) Color.White else Color(0xFF535353)
                    )
                }

                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White
                        )
                    } else {
                        FilledIconButton(
                            onClick = onPlayPauseClick,
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(36.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onNextClick,
                    enabled = canPlayNext && !isLoading,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(40.dp),
                        tint = if (canPlayNext) Color.White else Color(0xFF535353)
                    )
                }

                IconButton(
                    onClick = onRepeatClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    val repeatIcon = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.REPEAT_ALL -> Icons.Default.RepeatOn
                        RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                    }

                    val repeatTint = when (repeatMode) {
                        RepeatMode.OFF -> Color(0xFF808080)
                        RepeatMode.REPEAT_ALL -> MaterialTheme.colorScheme.primary
                        RepeatMode.REPEAT_ONE -> MaterialTheme.colorScheme.primary
                    }

                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Error banner with retry button
            if (error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Button(
                            onClick = onRetryClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                text = "Retry",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (showLyricsPanel) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Lyrics · ${song.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lyrics are not available yet for this track.\nWe'll show synced lyrics here in a future update.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueBottomSheet(
    queue: List<Song>,
    currentIndex: Int,
    onQueueItemClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY > 200) {
                                onDismiss()
                            } else {
                                offsetY = 0f
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            val newOffset = offsetY + dragAmount
                            if (newOffset >= 0) {
                                offsetY = newOffset
                            }
                        }
                    )
                }
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFF1C1C1E))
                .clickable(onClick = {}),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF535353))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Queue (${queue.size} songs)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Queue list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(queue) { index, song ->
                        QueueSongCard(
                            song = song,
                            isCurrentSong = index == currentIndex,
                            onClick = {
                                onQueueItemClick(index)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueSongCard(
    song: Song,
    isCurrentSong: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (isCurrentSong) Color(0x33FFFFFF) else Color.Transparent
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB3B3B3),
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyPlayerState(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎵",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No song playing",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Search and play a song to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB3B3B3),
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}

// ==================== DIALOGS ====================

@Composable
private fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit,
    onCreateNew: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            Column {
                // Create New Playlist Button
                Button(
                    onClick = onCreateNew,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create New Playlist")
                }

                Spacer(Modifier.height(16.dp))

                if (playlists.isEmpty()) {
                    Text(
                        text = "No playlists yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { _, playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onPlaylistSelected(playlist.id.toLong())
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = playlist.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                        Text(
                                            text = "${playlist.songs.size} songs",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = {
                    playlistName = it
                    showError = false
                },
                label = { Text("Playlist Name") },
                singleLine = true,
                isError = showError,
                supportingText = if (showError) {
                    { Text("Name cannot be empty") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isBlank()) {
                        showError = true
                    } else {
                        onConfirm(playlistName.trim())
                    }
                }
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
