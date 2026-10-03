package dev.socialmusic.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Spacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val section = 32.dp
}

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8E6CF), onPrimary = Color(0xFF0C3526),
    primaryContainer = Color(0xFF20483A), onPrimaryContainer = Color(0xFFD6F6E9),
    secondary = Color(0xFFA8E6CF), onSecondary = Color(0xFF0C3526),
    secondaryContainer = Color(0xFF20483A), onSecondaryContainer = Color(0xFFD6F6E9),
    background = Color(0xFF101419), onBackground = Color(0xFFF1F4F6),
    surface = Color(0xFF1B222B), onSurface = Color(0xFFF1F4F6),
    surfaceVariant = Color(0xFF29323D), onSurfaceVariant = Color(0xFFCDD4DE),
)
private val LightColors = lightColorScheme(
    primary = Color(0xFF176448), onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F0E5), onPrimaryContainer = Color(0xFF0C3526),
    secondary = Color(0xFF176448), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F0E5), onSecondaryContainer = Color(0xFF0C3526),
    background = Color(0xFFF5F7F8), onBackground = Color(0xFF151B22),
    surface = Color.White, onSurface = Color(0xFF151B22),
    surfaceVariant = Color(0xFFE2E8EB), onSurfaceVariant = Color(0xFF3D4854),
)

@Composable
fun SocialMusicTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
