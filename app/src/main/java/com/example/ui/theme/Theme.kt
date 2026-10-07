package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FloodSecondary,
    onPrimary = FloodOnSecondary,
    primaryContainer = FloodPrimaryContainer,
    onPrimaryContainer = FloodOnPrimaryContainer,
    secondary = FloodSecondary,
    onSecondary = FloodOnSecondary,
    secondaryContainer = FloodSecondaryContainer,
    onSecondaryContainer = FloodOnSecondaryContainer,
    tertiary = FloodTertiary,
    onTertiary = FloodOnTertiary,
    tertiaryContainer = FloodTertiaryContainer,
    onTertiaryContainer = FloodOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = FloodPrimary,
    onPrimary = FloodOnPrimary,
    primaryContainer = FloodPrimaryContainer,
    onPrimaryContainer = FloodOnPrimaryContainer,
    secondary = FloodSecondary,
    onSecondary = FloodOnSecondary,
    secondaryContainer = FloodSecondaryContainer,
    onSecondaryContainer = FloodOnSecondaryContainer,
    tertiary = FloodTertiary,
    onTertiary = FloodOnTertiary,
    tertiaryContainer = FloodTertiaryContainer,
    onTertiaryContainer = FloodOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

@Composable
fun FloodWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our high-contrast oceanic palette for critical safety
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
