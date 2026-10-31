package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VeilEmerald,
    onPrimary = Color(0xFF022C22),
    primaryContainer = VeilEmeraldContainer,
    onPrimaryContainer = VeilEmeraldOnContainer,
    secondary = VeilCyanAccent,
    onSecondary = Color(0xFF082F49),
    secondaryContainer = VeilCyanContainer,
    onSecondaryContainer = VeilCyanOnContainer,
    tertiary = VeilDarkTextSecondary,
    onTertiary = Color.White,
    background = VeilDarkBg,
    onBackground = VeilDarkTextPrimary,
    surface = VeilDarkSurface,
    onSurface = VeilDarkTextPrimary,
    surfaceVariant = VeilDarkSurfaceElevated,
    onSurfaceVariant = VeilDarkTextSecondary,
    outline = VeilDarkSurfaceBorder,
    error = VeilEmergencyRed,
    onError = Color.White,
    errorContainer = VeilEmergencyContainer,
    onErrorContainer = VeilEmergencyOnContainer
)

private val LightColorScheme = lightColorScheme(
    primary = VeilEmeraldDark,
    onPrimary = Color.White,
    primaryContainer = VeilEmeraldContainer,
    onPrimaryContainer = VeilEmeraldOnContainer,
    secondary = Color(0xFF0369A1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF082F49),
    tertiary = VeilLightTextSecondary,
    onTertiary = Color.White,
    background = VeilLightBg,
    onBackground = VeilLightTextPrimary,
    surface = VeilLightSurface,
    onSurface = VeilLightTextPrimary,
    surfaceVariant = VeilLightSurfaceElevated,
    onSurfaceVariant = VeilLightTextSecondary,
    outline = VeilLightSurfaceBorder,
    error = VeilEmergencyBright,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = VeilEmergencyDark
)

@Composable
fun VeilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We intentionally enforce our safety-tech dark/light scheme without dynamic tinting
    // because safety UI relies strictly on precise green (normal) and red (emergency) signals.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
