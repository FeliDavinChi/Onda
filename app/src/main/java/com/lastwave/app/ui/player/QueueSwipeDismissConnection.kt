package com.lastwave.app.ui.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/** Pull down after the queue has reached its top; leave ordinary list scrolling alone. */
internal class QueueSwipeDismissConnection(
    private val threshold: Float,
    private val onOffsetChange: (Float) -> Unit,
    private val onDraggingChange: (Boolean) -> Unit,
    private val onDismiss: () -> Unit,
) : NestedScrollConnection {
    private var dragOffset = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput || dragOffset == 0f) return Offset.Zero
        val previous = dragOffset
        updateOffset((previous + available.y).coerceAtLeast(0f))
        return Offset(0f, dragOffset - previous)
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput || available.y <= 0f) return Offset.Zero
        updateOffset(dragOffset + available.y)
        return Offset(0f, available.y)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (dragOffset == 0f) return Velocity.Zero
        finish()
        return Velocity(0f, available.y)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (dragOffset > 0f) finish()
        return Velocity.Zero
    }

    private fun updateOffset(value: Float) {
        dragOffset = value
        onOffsetChange(value)
        onDraggingChange(value > 0f)
    }

    private fun finish() {
        val shouldDismiss = dragOffset >= threshold
        updateOffset(0f)
        if (shouldDismiss) onDismiss()
    }

    fun cancel() {
        if (dragOffset > 0f) updateOffset(0f)
    }
}
