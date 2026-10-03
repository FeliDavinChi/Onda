package dev.socialmusic.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Spacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val section = 32.dp
}

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8E6CF), onPrimary = Color(0xFF0C3526),
    primaryContainer = Color(0xFF20483A), onPrimaryContainer = Color(0xFFD6F6E9),
    secondary = Color(0xFFB9D6E5), onSecondary = Color(0xFF173440),
    secondaryContainer = Color(0xFF20483A), onSecondaryContainer = Color(0xFFD6F6E9),
    background = Color(0xFF101419), onBackground = Color(0xFFF1F4F6),
    surface = Color(0xFF1B222B), onSurface = Color(0xFFF1F4F6),
    surfaceVariant = Color(0xFF29323D), onSurfaceVariant = Color(0xFFCDD4DE),
    outline = Color(0xFF63716F), outlineVariant = Color(0xFF34413F),
)
private val LightColors = lightColorScheme(
    primary = Color(0xFF176448), onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F0E5), onPrimaryContainer = Color(0xFF0C3526),
    secondary = Color(0xFF385F71), onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7F0E5), onSecondaryContainer = Color(0xFF0C3526),
    background = Color(0xFFF5F7F8), onBackground = Color(0xFF151B22),
    surface = Color.White, onSurface = Color(0xFF151B22),
    surfaceVariant = Color(0xFFE2E8EB), onSurfaceVariant = Color(0xFF3D4854),
    outline = Color(0xFF6B7975), outlineVariant = Color(0xFFCAD5D0),
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private val OndaFont = FontFamily(
    Font(R.font.manrope, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)
private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal, tracking: Float = 0f) =
    TextStyle(fontFamily = OndaFont, fontSize = size.sp, lineHeight = height.sp, fontWeight = weight, letterSpacing = tracking.sp)
private val OndaTypography = Typography(
    displayLarge = type(48, 54, FontWeight.SemiBold, -1.5f),
    displayMedium = type(40, 46, FontWeight.SemiBold, -1.1f),
    displaySmall = type(34, 40, FontWeight.SemiBold, -.8f),
    headlineLarge = type(30, 38, FontWeight.SemiBold, -.6f),
    headlineMedium = type(26, 34, FontWeight.SemiBold, -.5f),
    headlineSmall = type(23, 30, FontWeight.SemiBold, -.3f),
    titleLarge = type(21, 28, FontWeight.SemiBold, -.3f),
    titleMedium = type(16, 23, FontWeight.SemiBold),
    titleSmall = type(14, 20, FontWeight.SemiBold),
    bodyLarge = type(16, 24), bodyMedium = type(14, 21), bodySmall = type(12, 18),
    labelLarge = type(14, 20, FontWeight.SemiBold), labelMedium = type(12, 18, FontWeight.Medium), labelSmall = type(11, 16, FontWeight.Medium),
)

@Composable
fun SocialMusicTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (dark) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = OndaTypography) {
        CompositionLocalProvider(LocalContentColor provides colors.onBackground, content = content)
    }
}
