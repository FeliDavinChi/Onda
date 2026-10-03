package com.lastwave.app.ui.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class QueueSwipeDismissConnectionTest {
    @Test fun downwardPullAtListTopDismissesQueueOnRelease() = runTest {
        var dismissed = 0
        var offset = 0f
        var dragging = false
        val connection = QueueSwipeDismissConnection(88f, { offset = it }, { dragging = it }, { dismissed++ })
        val consumed = connection.onPostScroll(Offset.Zero, Offset(0f, 100f), NestedScrollSource.UserInput)
        assertEquals(100f, consumed.y, 0f)
        assertEquals(100f, offset, 0f)
        assertTrue(dragging)
        assertEquals(0, dismissed)
        connection.onPreFling(Velocity(0f, 300f))
        assertEquals(1, dismissed)
        assertEquals(0f, offset, 0f)
        assertFalse(dragging)
    }

    @Test fun ordinaryScrollingAndAutomaticScrollDoNotDismissQueue() = runTest {
        var dismissed = 0
        var offset = 0f
        val connection = QueueSwipeDismissConnection(88f, { offset = it }, {}, { dismissed++ })
        assertEquals(Offset.Zero, connection.onPostScroll(Offset(0f, 100f), Offset.Zero, NestedScrollSource.UserInput))
        assertEquals(Offset.Zero, connection.onPostScroll(Offset.Zero, Offset(0f, -100f), NestedScrollSource.UserInput))
        assertEquals(Offset.Zero, connection.onPostScroll(Offset.Zero, Offset(0f, 100f), NestedScrollSource.SideEffect))
        assertEquals(Velocity.Zero, connection.onPreFling(Velocity(0f, 500f)))
        assertEquals(0f, offset, 0f)
        assertEquals(0, dismissed)
    }

    @Test fun shortPullSpringsBackWithoutClosingQueue() = runTest {
        var dismissed = 0
        var offset = 0f
        val connection = QueueSwipeDismissConnection(88f, { offset = it }, {}, { dismissed++ })
        connection.onPostScroll(Offset.Zero, Offset(0f, 40f), NestedScrollSource.UserInput)
        assertEquals(40f, offset, 0f)
        connection.onPreFling(Velocity.Zero)
        assertEquals(0f, offset, 0f)
        assertEquals(0, dismissed)
    }

    @Test fun reversingPullReturnsRemainingScrollToSongList() = runTest {
        var dismissed = 0
        var offset = 0f
        val connection = QueueSwipeDismissConnection(88f, { offset = it }, {}, { dismissed++ })
        connection.onPostScroll(Offset.Zero, Offset(0f, 40f), NestedScrollSource.UserInput)
        val consumed = connection.onPreScroll(Offset(0f, -70f), NestedScrollSource.UserInput)
        assertEquals(-40f, consumed.y, 0f)
        assertEquals(0f, offset, 0f)
        connection.onPreFling(Velocity(0f, -200f))
        assertEquals(0, dismissed)
    }

    @Test fun leavingQueueCancelsPullWithoutChangingPlayerTab() {
        var dismissed = 0
        var dragging = false
        var offset = 0f
        val connection = QueueSwipeDismissConnection(88f, { offset = it }, { dragging = it }, { dismissed++ })
        connection.onPostScroll(Offset.Zero, Offset(0f, 100f), NestedScrollSource.UserInput)
        assertTrue(dragging)
        connection.cancel()
        assertFalse(dragging)
        assertEquals(0f, offset, 0f)
        assertEquals(0, dismissed)
    }
}
