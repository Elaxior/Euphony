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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
            downloadManager = AppContainer.provideDownloadManager(),
            lyricsFetcher = AppContainer.provideLyricsFetcher()
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
            lyrics = uiState.lyrics,
            isLyricsLoading = uiState.isLyricsLoading,
            lyricsError = uiState.lyricsError,
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
            onRetryClick = { viewModel.retrySong() } // Add retry
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
    lyrics: String?,
    isLyricsLoading: Boolean,
    lyricsError: String?,
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
    onRetryClick: () -> Unit // Add retry
) {
    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

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
            canSwipeNext = canPlayNext,
            canSwipePrevious = canPlayPrevious,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPreviousClick = onPreviousClick,
            onShuffleClick = onShuffleClick,
            onRepeatClick = onRepeatClick,
            onSeek = onSeek,
            onNavigateBack = onNavigateBack,
            onQueueClick = {
                showQueue = !showQueue
                if (showQueue) showLyrics = false
            },
            onLyricsClick = {
                showLyrics = !showLyrics
                if (showLyrics) showQueue = false
            },
            onFavoriteClick = onFavoriteClick, // NEW
            onDownloadClick = onDownloadClick, // NEW
            onAddToPlaylistClick = onAddToPlaylistClick, // NEW
            onRetryClick = onRetryClick // Pass retry
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

        if (showLyrics) {
            LyricsBottomSheet(
                song = song,
                lyrics = lyrics,
                isLoading = isLyricsLoading,
                error = lyricsError,
                onDismiss = { showLyrics = false }
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
    canSwipeNext: Boolean,
    canSwipePrevious: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onFavoriteClick: () -> Unit, // NEW
    onDownloadClick: () -> Unit, // NEW: Download action
    onAddToPlaylistClick: () -> Unit, // NEW
    onRetryClick: () -> Unit // Add retry
) {
    val playerRepository = remember { AppContainer.providePlayerRepository() }
    val currentPosition by playerRepository.currentPosition.collectAsState()
    val duration by playerRepository.duration.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isUserSeeking by remember { mutableStateOf(false) }

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
                            colorScheme.scrim.copy(alpha = 0.52f),
                            colorScheme.background.copy(alpha = 0.86f),
                            colorScheme.background.copy(alpha = 0.96f)
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = colorScheme.onSurface.copy(alpha = 0.72f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLyricsClick) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Lyrics",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onQueueClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large album art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                var horizontalDragTotal by remember(song.videoId) { mutableFloatStateOf(0f) }

                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .pointerInput(song.videoId, canSwipeNext, canSwipePrevious) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    horizontalDragTotal += dragAmount
                                },
                                onDragEnd = {
                                    when {
                                        horizontalDragTotal <= -120f && canSwipeNext -> onNextClick()
                                        horizontalDragTotal >= 120f && canSwipePrevious -> onPreviousClick()
                                    }
                                    horizontalDragTotal = 0f
                                },
                                onDragCancel = {
                                    horizontalDragTotal = 0f
                                }
                            )
                        },
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
                        color = colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = colorScheme.onSurfaceVariant,
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
                            tint = if (isFavorite) colorScheme.tertiary else colorScheme.onSurface,
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
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "$downloadProgress%",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurface,
                                fontSize = 8.sp
                            )
                        } else {
                            // Show download icon
                            IconButton(onClick = onDownloadClick) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                    contentDescription = if (isDownloaded) "Downloaded" else "Download",
                                    tint = if (isDownloaded) colorScheme.primary else colorScheme.onSurface,
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
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
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
                            thumbColor = colorScheme.primary,
                            activeTrackColor = colorScheme.primary,
                            inactiveTrackColor = colorScheme.onSurface.copy(alpha = 0.28f)
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
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(duration),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = colorScheme.onSurfaceVariant
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
                        tint = if (isShuffleEnabled) colorScheme.primary else colorScheme.onSurfaceVariant,
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
                        tint = if (canPlayPrevious) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }

                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = colorScheme.primary
                        )
                    } else {
                        FilledIconButton(
                            onClick = onPlayPauseClick,
                            modifier = Modifier.size(72.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(36.dp),
                                tint = colorScheme.onPrimary
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
                        tint = if (canPlayNext) colorScheme.onSurface else colorScheme.onSurface.copy(alpha = 0.35f)
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
                        RepeatMode.OFF -> colorScheme.onSurfaceVariant
                        RepeatMode.REPEAT_ALL -> colorScheme.primary
                        RepeatMode.REPEAT_ONE -> colorScheme.primary
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
                        containerColor = colorScheme.errorContainer.copy(alpha = 0.75f)
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
                            tint = colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = colorScheme.onErrorContainer,
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
        }
    }
}

@Composable
private fun LyricsBottomSheet(
    song: Song,
    lyrics: String?,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.scrim.copy(alpha = 0.52f))
            .clickable(onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(colorScheme.surface)
                .clickable(onClick = {})
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                        .background(colorScheme.outline)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lyrics",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "${song.title} • ${song.artist}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close lyrics",
                        tint = colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                lyrics != null -> {
                    Text(
                        text = lyrics,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                        color = colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "Lyrics are not available for this song yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
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
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.scrim.copy(alpha = 0.52f))
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
                .background(colorScheme.surface)
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
                        .background(colorScheme.outline)
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
                        color = colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Close",
                            tint = colorScheme.onSurface
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
                if (isCurrentSong) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent
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
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyPlayerState(onNavigateBack: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                        tint = colorScheme.onSurface,
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
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Search and play a song to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
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
