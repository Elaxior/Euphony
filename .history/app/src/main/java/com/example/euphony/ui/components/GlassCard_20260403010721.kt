package com.example.euphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.euphony.ui.theme.AccentPurple
import com.example.euphony.ui.theme.GlassSurface

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
            GlassSurface.copy(alpha = 0.78f),
            Color(0xFF17182E).copy(alpha = 0.84f)
        )
    )

    val edgeGlow = Brush.horizontalGradient(
        colors = listOf(
            AccentPurple.copy(alpha = glowIntensity),
            Color(0xFFE063B0).copy(alpha = glowIntensity * 0.45f)
        )
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(edgeGlow)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x85FFFFFF),
                        Color(0x20FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .background(glassBackground)
    ) {
        content()
    }
}
