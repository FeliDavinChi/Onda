package dev.socialmusic.domain.music

import dev.socialmusic.model.*
import org.junit.Assert.*
import org.junit.Test

class PlaybackQueueTest {
    private val a = Track(MusicId("youtube", "abcdefghijk"), "A", emptyList())
    private val b = Track(MusicId("youtube", "lmnopqrstuv"), "B", emptyList())

    @Test fun `duplicate tracks have independently editable occurrences`() {
        val queue = PlaybackQueue.fromTracks(listOf(a, a, b))
        assertEquals(3, queue.map { it.occurrenceId }.distinct().size)
        val state = PlaybackState(queue, 1, PlaybackStatus.PLAYING, positionMs = 1200)
        val moved = PlaybackQueue.move(state, queue[1].occurrenceId, 2)
        assertEquals(listOf("A", "B", "A"), moved.queue.map { it.track.title })
        assertEquals(queue[1].occurrenceId, moved.queue[moved.currentIndex].occurrenceId)
        assertEquals(1200L, moved.positionMs)
    }
    @Test fun `removing active occurrence advances and resets position`() {
        val q = PlaybackQueue.fromTracks(listOf(a, b, a))
        val changed = PlaybackQueue.remove(PlaybackState(q, 1, PlaybackStatus.PLAYING, positionMs = 9000), q[1].occurrenceId)
        assertEquals(1, changed.currentIndex)
        assertEquals(q[2].occurrenceId, changed.queue[1].occurrenceId)
        assertEquals(0L, changed.positionMs)
    }
    @Test fun `last removal produces idle empty state`() {
        val q = PlaybackQueue.fromTracks(listOf(a))
        val changed = PlaybackQueue.remove(PlaybackState(q, 0, PlaybackStatus.PLAYING), q[0].occurrenceId)
        assertTrue(changed.queue.isEmpty())
        assertEquals(-1, changed.currentIndex)
        assertEquals(PlaybackStatus.IDLE, changed.status)
    }
    @Test fun `unknown occurrence and invalid move do not change queue`() {
        val q = PlaybackQueue.fromTracks(listOf(a, b))
        val state = PlaybackState(q, 0)
        assertEquals(state, PlaybackQueue.move(state, q[0].occurrenceId, -1))
        assertEquals(state, PlaybackQueue.move(state, q[0].occurrenceId, 2))
        assertEquals(state, PlaybackQueue.remove(state, "missing"))
    }
    @Test fun `queue size is bounded`() {
        assertEquals(200, PlaybackQueue.fromTracks(List(205) { a }).size)
    }
}

