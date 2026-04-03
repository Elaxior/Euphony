package com.example.euphony.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun baseTypography(
    family: FontFamily,
    bodySize: Int,
    headingWeight: FontWeight,
    letterSpacing: Float
): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = headingWeight,
            fontSize = 56.sp,
            letterSpacing = letterSpacing.sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = headingWeight,
            fontSize = 34.sp,
            letterSpacing = letterSpacing.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = headingWeight,
            fontSize = 22.sp,
            letterSpacing = (letterSpacing / 2f).sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            letterSpacing = (letterSpacing / 2f).sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            letterSpacing = (letterSpacing / 2f).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = bodySize.sp,
            lineHeight = (bodySize + 8).sp,
            letterSpacing = letterSpacing.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (bodySize - 2).sp,
            lineHeight = (bodySize + 6).sp,
            letterSpacing = letterSpacing.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (bodySize - 4).sp,
            lineHeight = (bodySize + 4).sp,
            letterSpacing = letterSpacing.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            letterSpacing = letterSpacing.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = letterSpacing.sp
        )
    )
}

internal fun typographyForPreset(preset: EuphonyThemePreset): Typography {
    return when (preset) {
        EuphonyThemePreset.CLASSIC -> baseTypography(
            family = FontFamily.SansSerif,
            bodySize = 16,
            headingWeight = FontWeight.Bold,
            letterSpacing = 0.2f
        )

        EuphonyThemePreset.NORD -> baseTypography(
            family = FontFamily.Serif,
            bodySize = 16,
            headingWeight = FontWeight.SemiBold,
            letterSpacing = 0.15f
        )

        EuphonyThemePreset.DRACULA -> baseTypography(
            family = FontFamily.Default,
            bodySize = 16,
            headingWeight = FontWeight.ExtraBold,
            letterSpacing = 0.2f
        )

        EuphonyThemePreset.CATPPUCCIN -> baseTypography(
            family = FontFamily.Cursive,
            bodySize = 16,
            headingWeight = FontWeight.Bold,
            letterSpacing = 0.05f
        )

        EuphonyThemePreset.GRUVBOX -> baseTypography(
            family = FontFamily.Monospace,
            bodySize = 15,
            headingWeight = FontWeight.SemiBold,
            letterSpacing = 0.4f
        )
    }
}