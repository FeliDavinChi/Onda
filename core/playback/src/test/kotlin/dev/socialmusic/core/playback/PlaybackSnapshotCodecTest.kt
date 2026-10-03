package dev.socialmusic.core.playback

import dev.socialmusic.model.*
import org.junit.Assert.*
import org.junit.Test

class PlaybackSnapshotCodecTest {
    private val track = Track(MusicId("youtube", "abcdefghijk"), "Track", emptyList(), metadata = mapOf("stream" to "https://signed.example/audio?token=secret"))
    @Test fun `playing snapshot restores paused at current duplicate occurrence`() {
        val q = listOf(QueueEntry("first", track), QueueEntry("second", track))
        val restored = PlaybackSnapshotCodec.decode(PlaybackSnapshotCodec.encode(PlaybackState(q, 1, PlaybackStatus.PLAYING, positionMs = 2345, shuffle = true, repeat = RepeatMode.ALL)))
        assertEquals("second", restored.queue[restored.currentIndex].occurrenceId)
        assertEquals(PlaybackStatus.PAUSED, restored.status)
        assertEquals(2345L, restored.positionMs)
        assertTrue(restored.shuffle)
        assertEquals(RepeatMode.ALL, restored.repeat)
    }
    @Test fun `snapshot strips transient provider metadata`() {
        val encoded = PlaybackSnapshotCodec.encode(PlaybackState(listOf(QueueEntry("one", track)), 0))
        assertFalse(encoded.contains("token"))
        assertTrue(PlaybackSnapshotCodec.decode(encoded).currentTrack!!.metadata.isEmpty())
    }
    @Test fun `corrupt oversized or duplicate occurrence snapshots restore empty`() {
        assertEquals(PlaybackState(), PlaybackSnapshotCodec.decode("{"))
        assertEquals(PlaybackState(), PlaybackSnapshotCodec.decode("x".repeat(1_000_001)))
        val valid = PlaybackSnapshotCodec.encode(PlaybackState(listOf(QueueEntry("one", track), QueueEntry("two", track)), 0))
        assertEquals(PlaybackState(), PlaybackSnapshotCodec.decode(valid.replace("two", "one")))
    }
    @Test fun `negative stored position clamps and missing active occurrence selects first`() {
        val encoded = PlaybackSnapshotCodec.encode(PlaybackState(listOf(QueueEntry("one", track)), 0, positionMs = 10))
        val restored = PlaybackSnapshotCodec.decode(encoded.replace("\"positionMs\":10", "\"positionMs\":-5").replace("\"currentOccurrence\":\"one\"", "\"currentOccurrence\":\"missing\""))
        assertEquals(0L, restored.positionMs)
        assertEquals(0, restored.currentIndex)
    }
}

