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

private val BentoColorScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    tertiary = Surface3,
    background = Background,
    surface = Surface2,
    onPrimary = Surface3,
    onSecondary = Background,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = Surface1,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = AccentDelete
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark theme based on Bento grid
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = BentoColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
