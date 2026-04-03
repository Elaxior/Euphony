package com.example.euphony.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.euphony.domain.model.Song

/**
 * Premium search result card with glassmorphism
 */
@Composable
fun SearchResultCard(
    song: Song,
    onClick: () -> Unit,
    onAddToQueue: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 12.dp,
        glowIntensity = 0.2f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Song info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.padding(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color(0xFFB3B3B3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (song.duration > 0) {
                        Text(
                            text = " • ${formatDuration(song.duration)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = Color(0xFF808080)
                        )
                    }
                }
            }

            // More options button
            IconButton(
                onClick = {
                    if (onAddToQueue != null) {
                        showMenu = true
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color(0xFFB3B3B3),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to queue") },
                    onClick = {
                        showMenu = false
                        onAddToQueue?.invoke()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
