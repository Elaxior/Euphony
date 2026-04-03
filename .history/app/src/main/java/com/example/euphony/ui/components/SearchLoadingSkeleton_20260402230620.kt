package com.example.euphony.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Loading skeleton for search results
 */
@Composable
fun SearchLoadingSkeleton(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 1000f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail skeleton
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(brush)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title skeleton
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(16.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(brush)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Artist skeleton
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(14.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(brush)
                    )
                }
            }
        }
    }
}
