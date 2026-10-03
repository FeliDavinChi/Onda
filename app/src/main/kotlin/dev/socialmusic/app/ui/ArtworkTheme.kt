package dev.socialmusic.app.ui

import android.util.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import dev.socialmusic.designsystem.EffectLevel
import dev.socialmusic.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private val coverColors = LruCache<String, Color>(24)

/** A small cancellable software thumbnail; only colors, never full covers, enter our cache. */
@Composable
internal fun ArtworkTheme(track: Track?, effects: EffectLevel, content: @Composable () -> Unit) {
    if (effects == EffectLevel.Minimal) { content(); return }
    val base = MaterialTheme.colorScheme
    val context = LocalContext.current
    val url = track?.artwork
    val extracted by produceState<Color?>(initialValue = null, url) {
        value = null
        if (url != null) {
            value = coverColors.get(url) ?: try { withContext(Dispatchers.IO) {
                val result = SingletonImageLoader.get(context).execute(ImageRequest.Builder(context).data(url).size(64).allowHardware(false).build())
                if (result !is SuccessResult) null else withContext(Dispatchers.Default) {
                    val bitmap = result.image.toBitmap(64, 64)
                    val pixels = IntArray(bitmap.width * bitmap.height)
                    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                    extractCoverColor(pixels)?.also { ensureActive(); coverColors.put(url, it) }
                }
            } } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { null }
        }
    }
    val dark = base.background.luminance() < .5f
    val target = extracted?.let { paletteAccent(it, base.background, base.surface, base.surfaceVariant, dark) } ?: base.primary
    val accent by animateColorAsState(target, tween(if (effects == EffectLevel.Full) 220 else 0), label = "cover accent")
    // Text readability is rechecked during the transition too, including transitions through gray.
    val safeAccent = paletteAccent(accent, base.background, base.surface, base.surfaceVariant, dark)
    MaterialTheme(colorScheme = base.copy(primary = safeAccent, onPrimary = contrastForeground(safeAccent),
        primaryContainer = lerp(base.surface, safeAccent, .18f), onPrimaryContainer = base.onSurface,
        secondaryContainer = lerp(base.surface, safeAccent, .18f), onSecondaryContainer = base.onSurface), content = content)
}
