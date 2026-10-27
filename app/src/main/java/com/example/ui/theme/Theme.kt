package com.example.ui.theme

import android.app.Activity
import android.os.Build
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

private val LightColorScheme = lightColorScheme(
    primary = BlackPrimary,
    onPrimary = Color.White,
    primaryContainer = BlackSecondary,
    onPrimaryContainer = GoldPrimary,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = GoldLight,
    onSecondaryContainer = GoldDark,
    tertiary = GoldPrimary,
    onTertiary = Color.Black,
    background = BackgroundWhite,
    onBackground = TextPrimaryDark,
    surface = SurfaceWhite,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderLight
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = BlackSecondary,
    onPrimaryContainer = GoldPrimary,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = BlackTertiary,
    onSecondaryContainer = GoldLight,
    tertiary = GoldLight,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

@Composable
fun SmartCostProTheme(
    darkTheme: Boolean = false, // Always force light theme across all sections and settings
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
