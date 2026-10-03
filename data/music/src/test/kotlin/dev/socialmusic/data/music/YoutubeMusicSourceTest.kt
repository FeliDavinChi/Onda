package dev.socialmusic.data.music

import dev.socialmusic.domain.music.MusicSourceException
import dev.socialmusic.model.MusicId
import dev.socialmusic.model.RecommendationSeed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class YoutubeMusicSourceTest {
    private val id = MusicId("youtube", "abcdefghijk")
    private val adapter = FixtureExtractor()
    private val source = YoutubeMusicSource(adapter, nowMs = { 1_000_000 })

    @Test fun blankSearchDoesNotExtract() = runTest {
        assertTrue(source.search(" \n ").tracks.isEmpty())
        assertEquals(0, adapter.calls)
    }

    @Test fun searchMapsIncompleteMetadataWithoutInventingDurations() = runTest {
        adapter.results = listOf(YoutubeTrackData("abcdefghijk", "  ", null, null, "http://bad", -1))
        val track = source.search(" song ").tracks.single()
        assertEquals("song", adapter.query)
        assertEquals(id, track.id)
        assertEquals("Untitled track", track.title)
        assertTrue(track.artists.isEmpty())
        assertNull(track.artwork)
        assertNull(track.durationMs)
    }

    @Test fun searchMapsArtworkArtistAndSecondsToMilliseconds() = runTest {
        adapter.results = listOf(YoutubeTrackData("abcdefghijk", "Song", "Artist", "UC123", "https://img.example/art.jpg", 123))
        val track = source.search("song").tracks.single()
        assertEquals(123_000L, track.durationMs)
        assertEquals("Artist", track.artists.single().name)
        assertEquals("channel:UC123", track.artists.single().id.providerId)
        assertEquals("https://img.example/art.jpg", track.artwork)
    }

    @Test fun malformedAndForeignIdsFailBeforeExtraction() = runTest {
        for (badId in listOf(MusicId("demo", "abcdefghijk"), MusicId("youtube", "https://youtube.com/watch?v=abcdefghijk"), MusicId("youtube", "bad"))) {
            expect<MusicSourceException.NotFound> { source.getStream(badId) }
        }
        assertEquals(0, adapter.calls)
    }

    @Test fun searchDropsMalformedAndDuplicateResultsAndBoundsCount() = runTest {
        adapter.results = listOf(YoutubeTrackData("bad", "Invalid")) +
            (0..70).map { YoutubeTrackData("%011d".format(it), "Song $it") } +
            YoutubeTrackData("00000000000", "Duplicate")
        val tracks = source.search("song").tracks
        assertEquals(50, tracks.size)
        assertEquals(50, tracks.map { it.id }.distinct().size)
        assertEquals("Song 0", tracks.first().title)
    }

    @Test fun streamChoosesHighestBitrateSupportedUnexpiredHttpsAudio() = runTest {
        adapter.audio = listOf(
            YoutubeAudioData("http://media.example/a", "audio/mp4", 512),
            YoutubeAudioData("https://media.example/a?expire=900", "audio/mp4", 512),
            YoutubeAudioData("https://media.example/a", "video/mp4", 512),
            YoutubeAudioData("https://media.example/a", "audio/mp4", 512, progressive = false),
            YoutubeAudioData("https://media.example/a?expire=2000", "audio/webm", 160),
            YoutubeAudioData("https://media.example/b?expire=2000", "audio/mp4", 128),
        )
        val stream = source.getStream(id)
        assertEquals("https://media.example/a?expire=2000", stream.url)
        assertEquals("audio/webm", stream.mimeType)
        assertEquals(2_000_000L, stream.expiresAtEpochMs)
    }

    @Test fun noPlayableStreamFailsExplicitly() = runTest {
        adapter.audio = listOf(YoutubeAudioData("https://media.example/a?expire=900", "audio/mp4", 128))
        expect<MusicSourceException.StreamUnavailable> { source.getStream(id) }
    }

    @Test fun malformedExpiryAndEmbeddedCredentialsAreRejected() = runTest {
        adapter.audio = listOf(
            YoutubeAudioData("https://media.example/a?expire=not-a-time", "audio/mp4", 256),
            YoutubeAudioData("https://user:password@media.example/a", "audio/mp4", 128),
            YoutubeAudioData("https://media.example/a?expire=9223372036854775807", "audio/mp4", 64),
        )
        expect<MusicSourceException.StreamUnavailable> { source.getStream(id) }
    }

    @Test fun getTrackReturnsRequestedCanonicalIdentity() = runTest {
        adapter.results = listOf(YoutubeTrackData("abcdefghijk", "Direct track", durationSeconds = 180))
        assertEquals("Direct track", source.getTrack(id).title)
        assertEquals(180_000L, source.getTrack(id).durationMs)
    }

    @Test fun recommendationsUseRelatedSeedOrLiveDiscoverySearch() = runTest {
        adapter.results = listOf(YoutubeTrackData("12345678901", "Discovered"))
        assertEquals("Discovered", source.getRecommendations(RecommendationSeed()).single().title)
        assertEquals("new music", adapter.query)
        assertEquals("Discovered", source.getRecommendations(RecommendationSeed(trackIds = listOf(id))).single().title)
    }

    @Test fun networkFailureIsTypedAndContainsNoSignedUrl() = runTest {
        adapter.failure = IOException("Failed https://media.example/?token=secret")
        val failure = expect<MusicSourceException.ProviderUnavailable> { source.search("song") }
        assertFalse(failure.message.orEmpty().contains("secret"))
    }

    @Test fun cancellationIsNeverTranslatedToProviderFailure() = runTest {
        adapter.failure = CancellationException("cancelled")
        expect<CancellationException> { source.search("song") }
    }

    @Test fun relatedTracksExcludeSeedAndDuplicates() = runTest {
        adapter.results = listOf(YoutubeTrackData("abcdefghijk", "Seed"), YoutubeTrackData("12345678901", "Related"), YoutubeTrackData("12345678901", "Duplicate"))
        assertEquals(listOf("Related"), source.getRelatedTracks(id).map { it.title })
    }

    private suspend inline fun <reified T : Throwable> expect(block: suspend () -> Any?): T {
        try { block() } catch (error: Throwable) {
            if (error is T) return error
            throw error
        }
        throw AssertionError("Expected ${T::class.simpleName}")
    }

    private class FixtureExtractor : YoutubeExtraction {
        var calls = 0
        var query: String? = null
        var failure: Throwable? = null
        var results = emptyList<YoutubeTrackData>()
        var audio = emptyList<YoutubeAudioData>()
        private fun called() { calls++; failure?.let { throw it } }
        override fun search(query: String): List<YoutubeTrackData> { called(); this.query = query; return results }
        override fun track(videoId: String): YoutubeTrackData { called(); return results.first() }
        override fun streams(videoId: String): List<YoutubeAudioData> { called(); return audio }
        override fun related(videoId: String): List<YoutubeTrackData> { called(); return results }
    }
}
