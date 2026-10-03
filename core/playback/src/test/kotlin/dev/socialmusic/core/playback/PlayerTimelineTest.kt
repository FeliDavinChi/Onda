package dev.socialmusic.core.playback

import androidx.media3.exoplayer.ExoPlayer
import dev.socialmusic.domain.music.PlaybackQueue
import dev.socialmusic.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Exercise Media3's actual unprepared timeline, without network or speakers. */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class PlayerTimelineTest {
    private val track = Track(MusicId("youtube", "abcdefghijk"), "Song", emptyList(), metadata = mapOf("stream" to "https://audio.example/?token=secret"))
    @Test fun restoredDuplicateQueueStaysPausedAndMovePreservesActiveOccurrence() {
        val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication()).build()
        try {
            val q = PlaybackQueue.fromTracks(listOf(track, track, track))
            player.setMediaItems(q.map(SessionProtocol::item), 1, 2345)
            assertFalse(player.playWhenReady)
            assertEquals(PlaybackStatus.PAUSED, SessionProtocol.state(player).status)
            assertEquals(2345L, SessionProtocol.state(player).positionMs)
            player.moveMediaItem(1, 2)
            val moved = SessionProtocol.state(player)
            assertEquals(q[1].occurrenceId, moved.queue[moved.currentIndex].occurrenceId)
            assertEquals(2, moved.currentIndex)
            assertEquals(2345L, moved.positionMs)
            assertTrue(moved.queue.all { it.track.metadata.isEmpty() })
        } finally { player.release() }
    }
    @Test fun removingActiveAndFinalOccurrencesProducesCoherentTimeline() {
        val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication()).build()
        try {
            val q = PlaybackQueue.fromTracks(listOf(track, track, track))
            player.setMediaItems(q.map(SessionProtocol::item), 1, 9000)
            player.removeMediaItem(1)
            assertEquals(q[2].occurrenceId, player.currentMediaItem!!.mediaId)
            assertEquals(0L, SessionProtocol.state(player).positionMs)
            player.clearMediaItems()
            assertEquals(PlaybackState(), SessionProtocol.state(player))
        } finally { player.release() }
    }
    @Test fun queuePayloadRejectsArbitraryProviderAndOversizedInput() {
        try { SessionProtocol.tracks("x".repeat(250_001)); fail("Oversized payload accepted") } catch (_: IllegalArgumentException) { }
        val foreign = track.copy(id = MusicId("other", "abcdefghijk"))
        try { SessionProtocol.tracks(SessionProtocol.payload(listOf(foreign))); fail("Foreign provider accepted") } catch (_: IllegalArgumentException) { }
    }
}
