package dev.socialmusic.data.music

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.*
import org.junit.Test
import org.schabi.newpipe.extractor.downloader.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class NewPipeDownloaderTest {
    @Test fun forwardsPostBodyAndHeadersAndReturnsResponse() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("result").addHeader("X-Result", "yes"))
            val downloader = NewPipeDownloader()
            val response = downloader.execute(Request.newBuilder()
                .post(server.url("/extract").toString(), "body".toByteArray())
                .setHeader("User-Agent", "fixture-agent")
                .setHeader("Content-Type", "application/json").build())
            val request = server.takeRequest(2, TimeUnit.SECONDS)!!
            assertEquals("POST", request.method)
            assertEquals("body", request.body.readUtf8())
            assertEquals("fixture-agent", request.getHeader("User-Agent"))
            assertEquals("result", response.responseBody())
            assertEquals("yes", response.getHeader("X-Result"))
        }
    }

    @Test fun httpFailureDoesNotBecomeAnEmptySuccess() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(503).setBody("private upstream detail"))
            try {
                NewPipeDownloader().execute(Request.newBuilder().get(server.url("/").toString()).build())
                fail("HTTP failure accepted")
            } catch (error: IOException) {
                assertFalse(error.message.orEmpty().contains("private upstream detail"))
            }
        }
    }

    @Test fun coroutineCancellationCancelsInFlightHttpCall() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
            val downloader = NewPipeDownloader(client)
            val task = async(Dispatchers.IO) {
                runInterruptible { downloader.execute(Request.newBuilder().get(server.url("/").toString()).build()) }
            }
            assertNotNull(server.takeRequest(2, TimeUnit.SECONDS))
            val started = System.nanoTime()
            task.cancel()
            task.join()
            assertTrue("Cancelled extraction must not await network timeout", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 2_000)
            assertEquals(0, client.dispatcher.runningCallsCount())
        }
    }
}
