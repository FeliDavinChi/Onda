package dev.socialmusic.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/** Static contours inspired by a wave. No perpetual animation or artwork decoding. */
@Composable
fun WaveBackdrop(modifier: Modifier = Modifier, subtle: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    val minimal = LocalEffectLevel.current == EffectLevel.Minimal
    Canvas(modifier) {
        drawRect(colors.background)
        if (!minimal) {
            val strength = if (subtle) .055f else .11f
            drawRect(Brush.radialGradient(listOf(colors.primary.copy(alpha = strength), Color.Transparent),
                center = Offset(size.width * .95f, size.height * .45f), radius = size.width * .95f))
            drawRect(Brush.radialGradient(listOf(colors.secondary.copy(alpha = strength * 1.4f), Color.Transparent),
                center = Offset(size.width * .12f, size.height * .94f), radius = size.width * .8f))
            repeat(5) { index ->
                val y = size.height * (.36f + index * .055f)
                val path = Path().apply {
                    moveTo(size.width * .45f, y - size.height * .12f)
                    cubicTo(size.width * .8f, y - size.height * .25f, size.width * .82f, y + size.height * .2f, size.width * 1.18f, y + size.height * .08f)
                }
                drawPath(path, colors.primary.copy(alpha = strength * (1f - index * .1f)), style = Stroke(size.width * .0025f, cap = StrokeCap.Round))
            }
            val crest = Path().apply {
                moveTo(-size.width * .2f, size.height * .98f)
                cubicTo(size.width * .2f, size.height * .73f, size.width * .52f, size.height * 1.1f, size.width * 1.2f, size.height * .89f)
            }
            drawPath(crest, colors.primary.copy(alpha = strength * .7f), style = Stroke(size.width * .018f, cap = StrokeCap.Round))
        }
    }
}
