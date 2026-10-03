package dev.socialmusic.app.ui

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import dev.socialmusic.designsystem.EffectLevel
import dev.socialmusic.designsystem.LocalEffectLevel

/** Respect Android's system haptic setting; no vibration during scrolling or continuous seeking. */
@Composable
internal fun tactileAction(action: () -> Unit): () -> Unit {
    val view = LocalView.current
    val enabled = LocalEffectLevel.current != EffectLevel.Minimal
    return { if (enabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK); action() }
}
