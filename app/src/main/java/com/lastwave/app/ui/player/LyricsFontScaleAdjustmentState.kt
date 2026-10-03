package com.lastwave.app.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

@Composable
internal fun rememberLyricsFontScaleAdjustment(
    scale: Float,
    onCommit: (Float) -> Unit,
): Pair<Float, LyricsFontScaleAdjustment> {
    var previewScale by remember { mutableFloatStateOf(scale) }
    val currentCommit by rememberUpdatedState(onCommit)
    val adjustment = remember {
        LyricsFontScaleAdjustment(
            initialScale = scale,
            onPreview = { previewScale = it },
            onCommit = { currentCommit(it) },
        )
    }
    LaunchedEffect(scale) { adjustment.sync(scale) }
    return previewScale to adjustment
}
