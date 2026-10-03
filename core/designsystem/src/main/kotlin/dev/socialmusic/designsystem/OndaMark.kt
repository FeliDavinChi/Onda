package dev.socialmusic.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/** Onda's flowing three-line signature. Decorative; the adjacent wordmark carries its name. */
@Composable
fun OndaMark(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        repeat(3) { index ->
            val y = size.height * (.26f + index * .24f)
            val path = Path().apply {
                moveTo(size.width * .08f, y)
                cubicTo(size.width * .35f, y - size.height * .22f, size.width * .65f, y + size.height * .22f, size.width * .92f, y)
            }
            drawPath(path, color, style = Stroke(size.height * .065f, cap = StrokeCap.Round))
        }
    }
}
