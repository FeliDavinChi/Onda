package dev.socialmusic.data.music

import dev.socialmusic.model.MusicId
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Opt in explicitly with ONDA_LIVE_PROVIDER_TEST=true. Never silently substitute fixtures. */
class YoutubeLiveSmokeTest {
    @Test fun liveMusicSearch() = runBlocking {
        assumeTrue(System.getenv("ONDA_LIVE_PROVIDER_TEST") == "true")
        val tracks = YoutubeMusicSource().search("Kevin MacLeod Carefree").tracks
        assertTrue("Live music search returned no tracks", tracks.isNotEmpty())
        println("Live music search: ${tracks.size} tracks; first ID ${tracks.first().id.canonical}")
    }

    @Test fun livePublicTrackResolvesAndReturnsAudioBytes() = runBlocking {
        assumeTrue(System.getenv("ONDA_LIVE_PROVIDER_TEST") == "true")
        val stream = YoutubeMusicSource().getStream(MusicId("youtube", "dQw4w9WgXcQ"))
        assertTrue(stream.url.startsWith("https://"))
        val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
        client.newCall(Request.Builder().url(stream.url).header("Range", "bytes=0-1023").build()).execute().use { response ->
            assertTrue("Audio byte request failed with HTTP ${response.code}", response.isSuccessful)
            val bytes = response.body!!.byteStream().readNBytes(1024)
            assertTrue("Live stream returned no audio bytes", bytes.isNotEmpty())
            println("Live public track: ${bytes.size} audio bytes; MIME ${stream.mimeType}; HTTP ${response.code}")
        }
    }
}
