package com.example.euphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism card with blur effect and purple glow
 * Used for premium UI elements throughout the app
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    glowIntensity: Float = 0.3f,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val glassBackground = Brush.verticalGradient(
        colors = listOf(
            colorScheme.surfaceVariant.copy(alpha = 0.45f),
            colorScheme.surface.copy(alpha = 0.2f)
        )
    )

    val accentGlow = Brush.horizontalGradient(
        colors = listOf(
            colorScheme.primary.copy(alpha = glowIntensity),
            colorScheme.secondary.copy(alpha = glowIntensity * 0.55f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(accentGlow)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorScheme.onSurface.copy(alpha = 0.25f),
                        colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .background(glassBackground)
    ) {
        content()
    }
}
