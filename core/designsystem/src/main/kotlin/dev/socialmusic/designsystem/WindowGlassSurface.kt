package dev.socialmusic.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Popups and sheets own their capture: a Haze state must never span Android windows. */
@Composable
fun WindowGlassSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    GlassBackdropScope(LocalEffectLevel.current) {
        Box(modifier) {
            WaveBackdrop(Modifier.matchParentSize().glassBackdropSource(), subtle = true)
            GlassSurface(level = GlassLevel.Overlay, content = content)
        }
    }
}
