package dev.socialmusic.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassLevel { None, Subtle, Standard, Elevated, Overlay }
enum class EffectLevel { Full, Reduced, Minimal }
data class GlassStyle(val alpha: Float, val borderAlpha: Float, val elevation: Dp)

object GlassTokens {
    val cornerRadius = 28.dp
    val borderWidth = 1.dp
    fun style(level: GlassLevel, effects: EffectLevel): GlassStyle {
        if (effects == EffectLevel.Minimal) return GlassStyle(1f, 0f, 0.dp)
        val base = when (level) {
            GlassLevel.None -> GlassStyle(1f, 0f, 0.dp)
            GlassLevel.Subtle -> GlassStyle(.97f, .08f, 0.dp)
            GlassLevel.Standard -> GlassStyle(.94f, .12f, 2.dp)
            GlassLevel.Elevated -> GlassStyle(.92f, .16f, 6.dp)
            GlassLevel.Overlay -> GlassStyle(.96f, .18f, 10.dp)
        }
        return if (effects == EffectLevel.Reduced) base.copy(elevation = 0.dp) else base
    }
}

/** Readable tint/depth foundation. No foreground blur pretending to be backdrop glass. */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Standard,
    effects: EffectLevel = EffectLevel.Full,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = GlassTokens.style(level, effects)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GlassTokens.cornerRadius),
        color = MaterialTheme.colorScheme.surface.copy(alpha = style.alpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = style.elevation,
        border = if (style.borderAlpha == 0f) null else BorderStroke(
            GlassTokens.borderWidth, MaterialTheme.colorScheme.onSurface.copy(alpha = style.borderAlpha),
        ),
    ) { Box(content = content) }
}
