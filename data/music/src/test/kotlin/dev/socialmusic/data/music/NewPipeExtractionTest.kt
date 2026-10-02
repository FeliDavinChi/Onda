package dev.socialmusic.data.music

import org.junit.Assert.*
import org.junit.Test
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod

class NewPipeExtractionTest {
    private val extraction = NewPipeExtraction()

    @Test fun dashAudioUrlIsNotMarkedProgressive() {
        val stream = audio(DeliveryMethod.DASH)

        assertFalse(extraction.mapAudio(stream).progressive)
    }

    @Test fun progressiveAudioUrlPreservesPlaybackMetadata() {
        val mapped = extraction.mapAudio(audio(DeliveryMethod.PROGRESSIVE_HTTP))

        assertTrue(mapped.progressive)
        assertEquals("https://media.example/file.m4a", mapped.url)
        assertEquals("audio/mp4", mapped.mimeType)
        assertEquals(128, mapped.bitrate)
    }

    @Test fun inlineManifestIsNotMarkedProgressiveEvenWithProgressiveDelivery() {
        val stream = AudioStream.Builder()
            .setId("140")
            .setContent("<MPD></MPD>", false)
            .setMediaFormat(MediaFormat.M4A)
            .setAverageBitrate(128)
            .setDeliveryMethod(DeliveryMethod.PROGRESSIVE_HTTP)
            .build()

        assertFalse(extraction.mapAudio(stream).progressive)
    }

    private fun audio(delivery: DeliveryMethod): AudioStream = AudioStream.Builder()
        .setId("140")
        .setContent("https://media.example/file.m4a", true)
        .setMediaFormat(MediaFormat.M4A)
        .setAverageBitrate(128)
        .setDeliveryMethod(delivery)
        .build()
}
