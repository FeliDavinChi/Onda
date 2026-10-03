package dev.socialmusic.model

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ModelRoundTripTest {
    @Test fun cachedTrackRoundTripsWithoutLosingProviderIdentity() {
        val track = Track(
            MusicId("demo", "a:1"), "Original", listOf(ArtistRef(MusicId("demo", "artist"), "Artist")),
            durationMs = 123000, metadata = mapOf("genre" to "ambient"),
        )
        val snapshot = Json.encodeToString(Track.serializer(), track)
        assertEquals(track, Json.decodeFromString(Track.serializer(), snapshot))
    }
    @Test fun repeatedTrackNeedsSeparateQueueOccurrenceIds() {
        val track = Track(MusicId("demo", "one"), "Track", emptyList())
        val state = PlaybackState(queue = listOf(QueueEntry("a", track), QueueEntry("b", track)), currentIndex = 1)
        assertEquals(track, state.currentTrack)
        assertThrows(IllegalArgumentException::class.java) {
            PlaybackState(queue = listOf(QueueEntry("same", track), QueueEntry("same", track)))
        }
    }
    @Test fun queueIndexCannotPointPastTheEnd() {
        assertThrows(IllegalArgumentException::class.java) { PlaybackState(currentIndex = 0) }
        assertNull(PlaybackState().currentTrack)
    }
}
