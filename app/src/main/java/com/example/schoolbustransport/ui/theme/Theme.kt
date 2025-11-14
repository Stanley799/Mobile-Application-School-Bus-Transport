package com.example.schoolbustransport.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Our palette intentionally leans into a modern blue theme (Spotify/Duolingo-inspired),
// with SchoolBus yellow accents for CTAs and highlights.
private val DarkColorScheme = darkColorScheme(
    primary = SkyBlueNight,
    secondary = SchoolBusYellow,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkTextPrimary,
    onSecondary = TextPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBlue,
    secondary = SchoolBusYellow,
    background = LightGrey,
    surface = White,
    onPrimary = White,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SchoolBusTransportTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Push primary color to the system status bar for a cohesive look
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            // Toggle light/dark status bar icons to ensure contrast
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
