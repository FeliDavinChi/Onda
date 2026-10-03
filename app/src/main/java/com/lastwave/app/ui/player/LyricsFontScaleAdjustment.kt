package com.lastwave.app.ui.player

/** A drag changes the local preview; only its completion updates persisted lyrics. */
internal class LyricsFontScaleAdjustment(
    initialScale: Float,
    private val onPreview: (Float) -> Unit,
    private val onCommit: (Float) -> Unit,
) {
    private var value = initialScale
    private var pending = false

    fun preview(scale: Float) {
        value = scale
        pending = true
        onPreview(value)
    }

    fun finish() {
        if (!pending) return
        pending = false
        onCommit(value)
    }

    fun sync(scale: Float) {
        if (pending || scale == value) return
        value = scale
        onPreview(value)
    }

    fun select(scale: Float) {
        value = scale
        pending = false
        onPreview(value)
        onCommit(value)
    }
}
