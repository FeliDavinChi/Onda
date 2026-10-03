package dev.socialmusic.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** Small RGB buckets favor chromatic cover pixels over black, white and transparent borders. */
internal fun extractCoverColor(pixels: IntArray): Color? {
    data class Bucket(var count: Int = 0, var red: Long = 0, var green: Long = 0, var blue: Long = 0)
    val buckets = mutableMapOf<Int, Bucket>()
    pixels.forEach { argb ->
        val alpha = argb ushr 24
        val r = argb ushr 16 and 255; val g = argb ushr 8 and 255; val b = argb and 255
        val high = maxOf(r, g, b); val low = minOf(r, g, b)
        if (alpha >= 192 && high > 35 && low < 235 && high - low > 24) {
            val key = (r / 32 shl 6) or (g / 32 shl 3) or (b / 32)
            buckets.getOrPut(key) { Bucket() }.apply { count++; red += r; green += g; blue += b }
        }
    }
    val best = buckets.values.maxByOrNull { it.count } ?: return null
    return Color((best.red / best.count).toInt(), (best.green / best.count).toInt(), (best.blue / best.count).toInt())
}

internal fun colorContrast(a: Color, b: Color): Float =
    (maxOf(a.luminance(), b.luminance()) + .05f) / (minOf(a.luminance(), b.luminance()) + .05f)

internal fun readableAccent(extracted: Color, background: Color, dark: Boolean): Color {
    if (colorContrast(extracted, background) >= 4.5f) return extracted
    val endpoint = if (dark) Color.White else Color.Black
    for (step in 0..100) {
        val candidate = lerp(extracted, endpoint, step / 100f)
        if (colorContrast(candidate, background) >= 4.5f) return candidate
    }
    return endpoint
}

internal fun contrastForeground(color: Color): Color =
    if (colorContrast(Color.Black, color) >= colorContrast(Color.White, color)) Color.Black else Color.White

internal fun paletteAccent(extracted: Color, background: Color, surface: Color, variant: Color, dark: Boolean): Color {
    val endpoint = if (dark) Color.White else Color.Black
    for (step in 0..100) {
        val candidate = if (step == 0) extracted else lerp(extracted, endpoint, step / 100f)
        val container = lerp(surface, candidate, .18f)
        if (listOf(background, surface, variant, container).all { colorContrast(candidate, it) >= 4.5f }) return candidate
    }
    return endpoint
}
