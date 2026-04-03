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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.euphony.domain.model.Song

/**
 * Glassmorphic mini player at bottom of screen
 * Premium elevated look with purple glow
 */
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Progress bar
        val progress = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFF1A1A1A),
            strokeCap = StrokeCap.Round,
        )

        // Glass card container
        GlassCard(
            cornerRadius = 0.dp,
            glowIntensity = 0.4f,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onMiniPlayerClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Album art + song info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art with glow
                    GlassCard(
                        cornerRadius = 8.dp,
                        glowIntensity = 0.3f,
                        modifier = Modifier.size(48.dp)
                    ) {
                        AsyncImage(
                            model = song.thumbnailUrl,
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Song info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = Color(0xFFB3B3B3),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Play/Pause button
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
