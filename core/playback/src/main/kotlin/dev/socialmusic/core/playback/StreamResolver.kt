package dev.socialmusic.core.playback

import dev.socialmusic.model.*
import java.io.IOException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.*

class StreamResolutionException : IOException("Audio stream unavailable")
class StreamResolver(private val fetch: suspend (MusicId) -> StreamInfo, private val now: () -> Long = System::currentTimeMillis) {
    private data class Cached(val stream: StreamInfo, val fetchedAt: Long)
    private val cache = ConcurrentHashMap<MusicId, Cached>()
    private val generation = AtomicLong()
    private val jobs = ConcurrentHashMap.newKeySet<Job>()
    suspend fun resolve(id: MusicId): StreamInfo {
        if (id.provider != "youtube" || !Regex("[a-zA-Z0-9_-]{11}").matches(id.providerId)) throw StreamResolutionException()
        val expected = generation.get()
        cache[id]?.takeIf { now() - it.fetchedAt < 60_000 && (it.stream.expiresAtEpochMs ?: Long.MAX_VALUE) > now() + 30_000 }?.let { return it.stream }
        val job = Job(currentCoroutineContext()[Job])
        jobs.add(job)
        try {
            return withContext(job) {
                if (expected != generation.get()) throw CancellationException("Queue changed")
                repeat(2) {
                    val stream = fetch(id)
                    ensureActive()
                    if (expected != generation.get()) throw CancellationException("Queue changed")
                    val secure = runCatching { URI(stream.url).let { it.scheme == "https" && it.host != null && it.userInfo == null } }.getOrDefault(false)
                    if (!secure) throw StreamResolutionException()
                    if ((stream.expiresAtEpochMs ?: Long.MAX_VALUE) > now()) {
                        cache[id] = Cached(stream, now())
                        return@withContext stream
                    }
                }
                throw StreamResolutionException()
            }
        } finally { jobs.remove(job); job.cancel() }
    }
    fun invalidate() { generation.incrementAndGet(); cache.clear(); jobs.forEach { it.cancel() } }
    fun invalidate(id: MusicId) { cache.remove(id) }
}
class ExpiryRecovery {
    private val attempted = mutableSetOf<String>()
    fun allow(occurrenceId: String): Boolean = attempted.add(occurrenceId)
    fun reset() { attempted.clear() }
}

