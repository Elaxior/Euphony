package com.example.euphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.euphony.domain.model.Song

/**
 * Premium song card with glassmorphism and optimized image loading
 * Used in horizontal scrolling sections
 */
@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: ((Song) -> Unit)? = null,
    onAddToPlaylistClick: ((Song) -> Unit)? = null,
    showActions: Boolean = false
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        // Album art with rounded corners and optimized loading
        Box {
            GlassCard(
                cornerRadius = 12.dp,
                glowIntensity = 0.2f,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.thumbnailUrl)
                        .crossfade(150)
                        .memoryCacheKey(song.id)
                        .diskCacheKey(song.id)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(300)
                        .build(),
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            // Action buttons overlay
            if (showActions && (onFavoriteClick != null || onAddToPlaylistClick != null)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    onFavoriteClick?.let {
                        IconButton(
                            onClick = { it(song) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Song title
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Artist name
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Actions row (Add to Playlist)
        if (showActions && onAddToPlaylistClick != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onAddToPlaylistClick(song) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
