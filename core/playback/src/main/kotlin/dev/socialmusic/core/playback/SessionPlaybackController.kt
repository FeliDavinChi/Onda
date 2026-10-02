package dev.socialmusic.core.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.session.SessionResult
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

@UnstableApi
class SessionPlaybackController(context: Context) : PlaybackController, AutoCloseable {
    private val context = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(PlaybackState())
    override val state = mutableState.asStateFlow()
    private var controller: MediaController? = null
    private var connection: Deferred<MediaController>? = null
    private val commands = Mutex()
    private val playSequence = AtomicLong()
    private data class PlayIntent(val tracks: List<Track>, val index: Int, val sequence: Long)
    @Volatile private var pendingPlay: PlayIntent? = null
    private var commandError: PlaybackError? = null
    private fun publish(player: Player) {
        val actual = SessionProtocol.state(player)
        // A rejected queue edit does not change an already playing transport.
        mutableState.value = actual.copy(error = actual.error ?: commandError)
    }
    private val listener = object : Player.Listener { override fun onEvents(player: Player, events: Player.Events) { publish(player) } }
    init {
        submit { }
        scope.launch { while (isActive) { delay(500); controller?.let(::publish) } }
    }
    private suspend fun connected(): MediaController {
        controller?.let { return it }
        val pending = connection ?: scope.async {
            MediaController.Builder(context, SessionToken(context, ComponentName(context, PlaybackService::class.java)))
                .setListener(object : MediaController.Listener {
                    override fun onDisconnected(controller: MediaController) {
                        if (this@SessionPlaybackController.controller === controller) {
                            this@SessionPlaybackController.controller = null
                            connection = null
                            mutableState.value = mutableState.value.copy(status = PlaybackStatus.ERROR, error = PlaybackError("CONTROLLER_DISCONNECTED", true))
                            controller.removeListener(listener)
                            controller.release()
                        }
                    }
                }).buildAsync().await().also { controller = it; it.addListener(listener); mutableState.value = SessionProtocol.state(it) }
        }.also { connection = it }
        return try { pending.await() } catch (error: Exception) { connection = null; throw error }
    }
    private fun submit(action: suspend (MediaController) -> Unit) {
        scope.launch {
            try { commands.withLock { action(connected()) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { mutableState.value = mutableState.value.copy(status = PlaybackStatus.ERROR, error = PlaybackError("CONTROLLER_CONNECTION_FAILED", true)) }
        }
    }
    private fun custom(operation: String, tracks: List<Track>? = null, occurrence: String? = null, index: Int = 0, sequence: Long? = null) {
        val encoded = tracks?.let(SessionProtocol::payload)
        if (encoded != null && encoded.length > 250_000) {
            scope.launch { commandError = PlaybackError("QUEUE_TOO_LARGE", false); controller?.let(::publish) }
            return
        }
        submit { player ->
            if (sequence != null && sequence != playSequence.get()) return@submit
            val result = player.sendCustomCommand(SessionProtocol.command, Bundle().apply { putString("operation", operation); putString("tracks", encoded); putString("occurrence", occurrence); putInt("index", index) }).await()
            if (result.resultCode == SessionResult.RESULT_SUCCESS) {
                commandError = null
                if (operation == "play" && pendingPlay?.sequence == sequence) pendingPlay = null
            } else commandError = PlaybackError("QUEUE_COMMAND_FAILED", true)
            publish(player)
        }
    }
    override fun play(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty() || startIndex !in 0 until minOf(tracks.size, 200)) return
        val sequence = playSequence.incrementAndGet()
        pendingPlay = PlayIntent(tracks.take(200), startIndex, sequence)
        custom("play", tracks, index = startIndex, sequence = sequence)
    }
    override fun togglePlayPause() {
        if (state.value.error != null && pendingPlay != null) { retry(); return }
        submit {
            commandError = null
            if (it.playerError != null) { it.prepare(); it.play() }
            else if (it.playWhenReady) it.pause()
            else { if (it.playbackState == Player.STATE_IDLE) it.prepare(); it.play() }
        }
    }
    override fun seekTo(positionMs: Long) = submit { it.seekTo(positionMs.coerceAtLeast(0)) }
    override fun next() = submit { it.seekToNextMediaItem() }
    override fun previous() = submit { it.seekToPrevious() }
    override fun setShuffle(enabled: Boolean) = submit { it.shuffleModeEnabled = enabled }
    override fun setRepeat(mode: RepeatMode) = submit { it.repeatMode = when (mode) { RepeatMode.ONE -> Player.REPEAT_MODE_ONE; RepeatMode.ALL -> Player.REPEAT_MODE_ALL; RepeatMode.OFF -> Player.REPEAT_MODE_OFF } }
    override fun addToQueue(track: Track) = custom("add", listOf(track))
    override fun playNext(track: Track) = custom("next", listOf(track))
    override fun remove(occurrenceId: String) = custom("remove", occurrence = occurrenceId)
    override fun move(occurrenceId: String, toIndex: Int) = custom("move", occurrence = occurrenceId, index = toIndex)
    override fun retry() { pendingPlay?.let { play(it.tracks, it.index); return }; custom("retry") }
    override fun clear() { playSequence.incrementAndGet(); pendingPlay = null; custom("clear") }
    override fun close() {
        scope.cancel(); connection?.cancel(); connection = null
        controller?.let { it.removeListener(listener); it.release() }; controller = null
    }
}
