package dev.socialmusic.core.playback

import android.util.AtomicFile
import dev.socialmusic.model.PlaybackState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/** One ordered store across service stop/rebind; only the latest pending payload is retained. */
internal object SnapshotStorage {
    private sealed interface Operation {
        data class Drain(val file: File) : Operation
        data class Read(val file: File, val result: CompletableDeferred<PlaybackState>) : Operation
    }
    private val pending = ConcurrentHashMap<String, String>()
    private val operations = Channel<Operation>(Channel.UNLIMITED)
    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            for (operation in operations) when (operation) {
                is Operation.Drain -> pending.remove(operation.file.absolutePath)?.let { encoded ->
                    val atomic = AtomicFile(operation.file)
                    var output: java.io.FileOutputStream? = null
                    try { output = atomic.startWrite(); output.write(encoded.toByteArray(Charsets.UTF_8)); atomic.finishWrite(output) }
                    catch (_: IOException) { output?.let(atomic::failWrite) }
                }
                is Operation.Read -> operation.result.complete(runCatching {
                    AtomicFile(operation.file).openRead().use { input ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (output.size() <= 1_000_000) {
                            val count = input.read(buffer, 0, minOf(buffer.size, 1_000_001 - output.size()))
                            if (count < 0) break
                            output.write(buffer, 0, count)
                        }
                        if (output.size() > 1_000_000) PlaybackState() else PlaybackSnapshotCodec.decode(output.toString("UTF-8"))
                    }
                }.getOrDefault(PlaybackState()))
            }
        }
    }
    fun write(file: File, encoded: String) {
        if (encoded.toByteArray(Charsets.UTF_8).size > 1_000_000) return
        pending[file.absolutePath] = encoded
        operations.trySend(Operation.Drain(file))
    }
    suspend fun read(file: File): PlaybackState {
        val result = CompletableDeferred<PlaybackState>()
        operations.send(Operation.Read(file, result))
        return result.await()
    }
}
