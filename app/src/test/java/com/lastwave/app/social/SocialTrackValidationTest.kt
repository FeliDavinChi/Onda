package com.lastwave.app.social

import org.junit.Assert.*
import org.junit.Test

class SocialTrackValidationTest {
    @Test fun sharedMusicCannotSupplyAStreamOrLocalFile() {
        for (value in listOf("https://example.com/audio", "file:///private/song", "../song", "short")) {
            assertThrows(IllegalArgumentException::class.java) { sharedTrack(value, "Title", "Artist") }
        }
    }
    @Test fun aShareResolvesMetadataThroughTheExistingPlayer() {
        val track = sharedTrack("abcdefghijk", "Title", "Artist")
        assertEquals("abcdefghijk", track.videoId)
        assertNull(track.playbackUrl)
        assertThrows(IllegalArgumentException::class.java) { sharedTrack("abcdefghijk", "", "Artist") }
    }
}
