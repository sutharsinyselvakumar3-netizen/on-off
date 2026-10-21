package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkNavyColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Navy950,
    primaryContainer = DarkGreen,
    onPrimaryContainer = LightGreen,
    secondary = NeonCyan,
    onSecondary = Navy950,
    secondaryContainer = CyanDark,
    onSecondaryContainer = Color(0xFFA5F3FC),
    tertiary = EmeraldGreen,
    onTertiary = Navy950,
    background = Navy950,
    onBackground = TextPrimary,
    surface = Navy900,
    onSurface = TextPrimary,
    surfaceVariant = Navy800,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    outlineVariant = Navy700,
    error = OffRed,
    onError = Navy950,
    errorContainer = OffRedDark,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // IoT controller has dedicated dark navy aesthetic
    dynamicColor: Boolean = false, // Keep intentional dark navy & vivid green theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkNavyColorScheme,
        typography = Typography,
        content = content
    )
}
