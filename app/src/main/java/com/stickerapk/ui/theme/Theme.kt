package com.stickerapk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkBackground = Color(0xFF0B0B10)
private val DarkSurface = Color(0xFF15151D)
private val DarkSurfaceVariant = Color(0xFF1F1F2B)
private val DarkPrimary = Color(0xFF8B5CF6)
private val DarkSecondary = Color(0xFF22D3EE)
private val DarkOnBackground = Color(0xFFF4F4F8)
private val DarkOnSurface = Color(0xFFE8E8F0)

private val LightBackground = Color(0xFFF7F7FB)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFECECF4)
private val LightPrimary = Color(0xFF6D28D9)
private val LightSecondary = Color(0xFF0891B2)
private val LightOnBackground = Color(0xFF12121A)
private val LightOnSurface = Color(0xFF1C1C28)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF042F2E),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB8B8C8),
    outline = Color(0xFF3A3A4D),
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4B4B5C),
    outline = Color(0xFFC9C9D6),
)

@Composable
fun StickerApkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
