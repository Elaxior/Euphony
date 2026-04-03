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
    val glassBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0x33FFFFFF), // Semi-transparent white top
            Color(0x1AFFFFFF)  // More transparent bottom
        )
    )

    val purpleGlow = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF8B5CF6).copy(alpha = glowIntensity),
            Color(0xFFEC4899).copy(alpha = glowIntensity * 0.5f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(purpleGlow)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x40FFFFFF),
                        Color(0x10FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .background(glassBackground)
    ) {
        content()
    }
}
