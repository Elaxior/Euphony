package com.example.euphony.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Loading skeleton for SongCard with shimmer animation
 */
@Composable
fun SongCardSkeleton(modifier: Modifier = Modifier) {
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
            .width(140.dp)
            .padding(4.dp)
    ) {
        // Image skeleton (square)
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Artist skeleton (70% width)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}
