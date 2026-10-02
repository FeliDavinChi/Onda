package dev.socialmusic.data.music

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** NewPipe is synchronous; interruption cancels its underlying HTTP call. */
internal class NewPipeDownloader(private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS).build()) : Downloader() {
    override fun execute(request: Request): Response {
        val builder = okhttp3.Request.Builder().url(request.url())
        request.headers().forEach { (name, values) -> values.forEach { builder.addHeader(name, it) } }
        if (request.headers().keys.none { it.equals("User-Agent", true) }) builder.header("User-Agent", "Mozilla/5.0")
        val body = request.dataToSend()?.toRequestBody() ?: if (request.httpMethod() in listOf("POST", "PUT", "PATCH")) ByteArray(0).toRequestBody() else null
        val call = client.newCall(builder.method(request.httpMethod(), body).build())
        val completed = CountDownLatch(1)
        var result: Response? = null
        var failure: IOException? = null
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { failure = IOException("Provider request failed"); completed.countDown() }
            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    response.use {
                        if (!it.isSuccessful) throw IOException("Provider HTTP ${it.code}")
                        result = Response(it.code, it.message, it.headers.toMultimap(), it.body?.string().orEmpty(), it.request.url.toString())
                    }
                } catch (_: IOException) { failure = IOException("Provider response failed") }
                finally { completed.countDown() }
            }
        })
        try { completed.await() } catch (interrupted: InterruptedException) {
            call.cancel()
            completed.await(1, TimeUnit.SECONDS)
            throw interrupted
        }
        failure?.let { throw it }
        return result ?: throw IOException("Provider response missing")
    }
}
