package dev.socialmusic.data.music

import dev.socialmusic.domain.music.MusicSourceException
import dev.socialmusic.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DemoMusicSourceTest {
    private val source = DemoMusicSource()
    @Test fun searchTrimsAndIgnoresCase() = runTest {
        assertEquals("Afterglow", source.search("  AFTERGLOW ").tracks.single().title)
    }
    @Test fun searchByArtistUsesSharedTrackModels() = runTest {
        val track = source.search("Daylight").tracks.single()
        assertEquals(source.getTrack(track.id), track)
    }
    @Test fun emptyQueryHasNoResults() = runTest { assertTrue(source.search(" ").tracks.isEmpty()) }
    @Test fun noMatchHasNoResults() = runTest { assertTrue(source.search("nonexistent").tracks.isEmpty()) }
    @Test fun missingIdentityIsRejected() = runTest {
        try { source.getTrack(MusicId("demo", "missing")); fail("Unknown identity accepted") }
        catch (_: MusicSourceException.NotFound) { }
    }
    @Test fun differentProviderIsNotResolvedAccidentally() = runTest {
        try { source.getTrack(MusicId("youtube", "track-afterglow")); fail("Wrong provider accepted") }
        catch (_: MusicSourceException.NotFound) { }
    }
    @Test fun demoNeverClaimsCommercialPlayback() = runTest {
        try { source.getStream(MusicId("demo", "track-afterglow")); fail("Demo stream unexpectedly exists") }
        catch (_: MusicSourceException.StreamUnavailable) { }
    }
    @Test fun relatedTracksExcludeTheSeedAndAreDeterministic() = runTest {
        val id = MusicId("demo", "track-afterglow")
        val related = source.getRelatedTracks(id)
        assertFalse(related.any { it.id == id })
        assertEquals(related, source.getRelatedTracks(id))
    }
    @Test fun searchFindsAlbumAndArtistEntities() = runTest {
        assertEquals("Open Windows", source.search("Open Windows").albums.single().title)
        assertEquals("Daylight Assembly", source.search("Daylight").artists.single().name)
    }
    @Test fun playlistHasSharedCanonicalTracks() = runTest {
        assertEquals(source.getRecommendations(RecommendationSeed()), source.getPlaylist(MusicId("demo", "playlist-first-light")).tracks)
    }
}
