package dev.socialmusic.designsystem

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

enum class GlassLevel { None, Subtle, Standard, Elevated, Overlay }
enum class EffectLevel { Full, Reduced, Minimal }
data class GlassStyle(val alpha: Float, val borderAlpha: Float, val elevation: Dp, val blur: Dp)

val LocalEffectLevel = staticCompositionLocalOf { EffectLevel.Full }
private data class GlassBackdrop(val state: HazeState, val enabled: Boolean)
private val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdrop?> { null }

object GlassTokens {
    val cornerRadius = 28.dp
    val borderWidth = 1.dp
    fun style(level: GlassLevel, effects: EffectLevel): GlassStyle {
        if (effects == EffectLevel.Minimal || level == GlassLevel.None) return GlassStyle(1f, 0f, 0.dp, 0.dp)
        val base = when (level) {
            GlassLevel.None -> GlassStyle(1f, 0f, 0.dp, 0.dp)
            GlassLevel.Subtle -> GlassStyle(.90f, .08f, 0.dp, 12.dp)
            GlassLevel.Standard -> GlassStyle(.78f, .14f, 2.dp, 18.dp)
            GlassLevel.Elevated -> GlassStyle(.72f, .22f, 8.dp, 24.dp)
            GlassLevel.Overlay -> GlassStyle(.88f, .24f, 10.dp, 24.dp)
        }
        return if (effects == EffectLevel.Reduced) base.copy(alpha = .94f, elevation = 0.dp, blur = 8.dp) else base
    }
}

/** One capture per window, behind floating surfaces. Never capture a glass effect itself. */
@Composable
fun GlassBackdropScope(effects: EffectLevel, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val lowRam = remember(context) {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice ?: true
    }
    val state = remember { HazeState() }
    val backdrop = remember(state, effects, lowRam) {
        // Haze 1.5.x enables its RenderEffect path on Android 13+. Earlier OSs stay solid.
        GlassBackdrop(state, Build.VERSION.SDK_INT >= 33 && !lowRam && effects != EffectLevel.Minimal)
    }
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop, LocalEffectLevel provides effects, content = content)
}

@Composable
fun Modifier.glassBackdropSource(): Modifier {
    val backdrop = LocalGlassBackdrop.current
    return if (backdrop?.enabled == true) hazeSource(backdrop.state) else this
}

/** Captured-background blur with tint and edge light. Foreground content remains sharp. */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Standard,
    effects: EffectLevel = LocalEffectLevel.current,
    radius: Dp = GlassTokens.cornerRadius,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = GlassTokens.style(level, effects)
    val backdrop = LocalGlassBackdrop.current
    val surface = MaterialTheme.colorScheme.surface
    val edge = MaterialTheme.colorScheme.onSurface
    val useBlur = backdrop?.enabled == true && style.blur > 0.dp
    val material = if (useBlur) Modifier.hazeEffect(
        state = backdrop!!.state,
        style = HazeStyle(backgroundColor = surface, tint = HazeTint(surface.copy(alpha = style.alpha)),
            blurRadius = style.blur, noiseFactor = 0f, fallbackTint = HazeTint(surface)),
    ) else Modifier.background(surface)
    Surface(modifier, shape = RoundedCornerShape(radius), color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface, shadowElevation = style.elevation) {
        Box(material.drawWithCache {
            val rim = Brush.linearGradient(
                listOf(edge.copy(alpha = style.borderAlpha), edge.copy(alpha = .025f), edge.copy(alpha = style.borderAlpha * .5f)),
                Offset.Zero, Offset(size.width, size.height),
            )
            onDrawWithContent {
                drawContent()
                if (style.borderAlpha > 0f) drawRoundRect(rim, cornerRadius = CornerRadius(radius.toPx()), style = Stroke(GlassTokens.borderWidth.toPx()))
            }
        }, content = content)
    }
}
