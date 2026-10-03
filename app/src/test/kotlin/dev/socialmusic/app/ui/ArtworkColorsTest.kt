package dev.socialmusic.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.junit.Assert.*
import org.junit.Test

class ArtworkColorsTest {
    @Test fun visibleChromaticArtWinsOverTransparentAndWhiteEdges() {
        val pixels = IntArray(100) { when { it < 20 -> 0x00FF0000; it < 40 -> 0xFFFFFFFF.toInt(); else -> 0xFF206FA0.toInt() } }
        val color = extractCoverColor(pixels)!!
        assertTrue(color.blue > color.red)
        assertTrue(color.green > color.red)
    }
    @Test fun neutralOrMissingArtUsesBrandFallback() {
        assertNull(extractCoverColor(intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0x004477AA)))
        assertNull(extractCoverColor(intArrayOf()))
    }
    @Test fun accentsAndForegroundsStayReadableInBothThemes() {
        listOf(Color(0xFF172739), Color(0xFFFFDD32), Color(0xFFCC2444), Color(0xFF22BBEE)).forEach { extracted ->
            listOf(true, false).forEach { dark ->
                val background = if (dark) Color(0xFF101419) else Color(0xFFF5F7F8)
                val accent = readableAccent(extracted, background, dark)
                assertTrue(colorContrast(accent, background) >= 4.5f)
                assertTrue(colorContrast(contrastForeground(accent), accent) >= 4.5f)
            }
        }
    }
    @Test fun selectedAndFloatingSurfacesAlsoKeepNormalTextContrast() {
        listOf(true, false).forEach { dark ->
            val background = if (dark) Color(0xFF101419) else Color(0xFFF5F7F8)
            val surface = if (dark) Color(0xFF1B222B) else Color.White
            val variant = if (dark) Color(0xFF29323D) else Color(0xFFE2E8EB)
            listOf(Color(0xFFB8734A), Color(0xFF22BBEE), Color(0xFF773388), Color(0xFFFFFF11)).forEach { cover ->
                val accent = paletteAccent(cover, background, surface, variant, dark)
                listOf(background, surface, variant, lerp(surface, accent, .18f)).forEach {
                    assertTrue("Accent text must be readable on every actual surface", colorContrast(accent, it) >= 4.5f)
                }
            }
        }
    }
}
