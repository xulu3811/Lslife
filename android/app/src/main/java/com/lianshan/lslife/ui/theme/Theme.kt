package com.lianshan.lslife.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = WarmInk,
    secondary = WarmGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E7C8),
    onSecondaryContainer = WarmInk,
    tertiary = MossGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6EBDD),
    onTertiaryContainer = WarmInk,
    background = WarmCream,
    onBackground = WarmInk,
    surface = WarmPaper,
    onSurface = WarmInk,
    surfaceVariant = WarmSand,
    onSurfaceVariant = WarmMuted,
    outline = WarmOutline,
    outlineVariant = Color(0xFFE8DFD4),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = TerracottaSoft,
    onPrimary = Color(0xFF2A1608),
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = TerracottaContainer,
    secondary = WarmGoldLight,
    onSecondary = Color(0xFF2A2108),
    secondaryContainer = Color(0xFF4A3B12),
    onSecondaryContainer = WarmGoldLight,
    tertiary = MossGreenLight,
    onTertiary = Color(0xFF0F2A1A),
    tertiaryContainer = Color(0xFF244833),
    onTertiaryContainer = MossGreenLight,
    background = WarmCharcoal,
    onBackground = WarmInkDark,
    surface = WarmCardDark,
    onSurface = WarmInkDark,
    surfaceVariant = WarmSurfaceVariantDark,
    onSurfaceVariant = WarmMutedDark,
    outline = WarmOutlineDark,
    outlineVariant = Color(0xFF4A4038),
    error = ErrorRedLight,
    onError = Color(0xFF3B0A08),
    errorContainer = Color(0xFF7A1E16),
    onErrorContainer = ErrorRedLight,
)

@Composable
fun LsLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LsTypography,
        shapes = LsShapes,
        content = content,
    )
}
