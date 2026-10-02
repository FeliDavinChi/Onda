package dev.socialmusic.core.playback

import dev.socialmusic.model.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class StreamResolverTest {
    private val id = MusicId("youtube", "abcdefghijk")
    @Test fun `expired stream is refreshed once and then rejected`() = runBlocking {
        var calls = 0
        val resolver = StreamResolver({ calls++; StreamInfo("https://audio.example/file", "audio/mp4", 99) }, { 100 })
        try { resolver.resolve(id); fail("expired stream accepted") } catch (_: StreamResolutionException) { }
        assertEquals(2, calls)
    }
    @Test fun `foreign IDs and insecure streams are rejected`() = runBlocking {
        var calls = 0
        val resolver = StreamResolver({ calls++; StreamInfo("http://audio.example/file", "audio/mp4", null) })
        try { resolver.resolve(MusicId("other", "abcdefghijk")); fail("foreign ID accepted") } catch (_: StreamResolutionException) { }
        assertEquals(0, calls)
        try { resolver.resolve(id); fail("HTTP accepted") } catch (_: StreamResolutionException) { }
        assertEquals(1, calls)
    }
    @Test fun `new generation cancels late resolution`() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val finish = CompletableDeferred<Unit>()
        val resolver = StreamResolver({ started.complete(Unit); finish.await(); StreamInfo("https://audio.example/file", "audio/mp4", null) })
        val pending = async { runCatching { resolver.resolve(id) } }
        started.await()
        resolver.invalidate()
        finish.complete(Unit)
        assertTrue(pending.await().exceptionOrNull() is CancellationException)
    }
    @Test fun `cached stream refreshes when approaching expiry`() = runBlocking {
        var time = 100L
        var calls = 0
        val resolver = StreamResolver({ calls++; StreamInfo("https://audio.example/$calls", "audio/mp4", 100_000) }, { time })
        assertEquals("https://audio.example/1", resolver.resolve(id).url)
        assertEquals("https://audio.example/1", resolver.resolve(id).url)
        time = 80_000
        assertEquals("https://audio.example/2", resolver.resolve(id).url)
    }
    @Test fun `expiry recovery is bounded per active occurrence`() {
        val recovery = ExpiryRecovery()
        assertTrue(recovery.allow("one"))
        assertFalse(recovery.allow("one"))
        assertTrue(recovery.allow("two"))
        recovery.reset()
        assertTrue(recovery.allow("one"))
    }
}
