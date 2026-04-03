package com.example.euphony.ui.screens.downloads

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.euphony.data.local.entity.DownloadEntity
import com.example.euphony.di.AppContainer
import com.example.euphony.ui.components.GradientBackground
import kotlin.math.pow

@Composable
fun DownloadsScreen(
    onNavigateToPlayer: () -> Unit = {}
) {
    val viewModel = remember {
        DownloadsViewModel(
            getDownloadsUseCase = AppContainer.provideGetDownloadsUseCase(),
            deleteDownloadUseCase = AppContainer.provideDeleteDownloadUseCase(),
            playSongUseCase = AppContainer.providePlaySongUseCase(),
            queueRepository = AppContainer.provideFakeQueueRepository(),
            downloadManager = AppContainer.provideDownloadManager()
        )
    }

    val uiState by viewModel.uiState.collectAsState()

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmDialog && uiState.downloadToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmDialog() },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete Download?") },
            text = {
                Text("Are you sure you want to delete \"${uiState.downloadToDelete!!.title}\"? The downloaded file will be permanently removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDownload(uiState.downloadToDelete!!.videoId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success/Error Messages
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccessMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            DownloadsContent(
                downloads = uiState.downloads,
                totalSize = uiState.totalSize,
                onSongClick = { download ->
                    viewModel.playSong(download)
                    onNavigateToPlayer()
                },
                onSongLongClick = { download ->
                    viewModel.showDeleteConfirmDialog(download)
                },
                onPlayAll = {
                    viewModel.playAllDownloads()
                    onNavigateToPlayer()
                },
                onShuffle = {
                    viewModel.shuffleDownloads()
                    onNavigateToPlayer()
                }
            )
        }

        // Snackbars
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(error)
            }
        }

        uiState.successMessage?.let { message ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(message)
            }
        }
    }
}

@Composable
private fun DownloadsContent(
    downloads: List<DownloadEntity>,
    totalSize: Long,
    onSongClick: (DownloadEntity) -> Unit,
    onSongLongClick: (DownloadEntity) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Offline Downloads",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "${downloads.size} songs • ${formatFileSize(totalSize)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (downloads.isEmpty()) {
                EmptyDownloadsState()
            } else {
                // Play controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, "Play All")
                        Spacer(Modifier.width(8.dp))
                        Text("Play All")
                    }

                    OutlinedButton(
                        onClick = onShuffle,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shuffle, "Shuffle")
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle")
                    }
                }

                // Downloads list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(downloads, key = { it.videoId }) { download ->
                        DownloadCard(
                            download = download,
                            onClick = { onSongClick(download) },
                            onLongClick = { onSongLongClick(download) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadCard(
    download: DownloadEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(download.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = download.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                )

                // Offline indicator
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OfflinePin,
                        contentDescription = "Downloaded",
                        modifier = Modifier
                            .padding(2.dp)
                            .size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = download.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "• ${formatFileSize(download.fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play icon
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun EmptyDownloadsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No offline songs",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Download songs to listen offline\nTap the download icon on any song",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Format file size in human-readable format
 */
private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return "%.1f %s".format(
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

