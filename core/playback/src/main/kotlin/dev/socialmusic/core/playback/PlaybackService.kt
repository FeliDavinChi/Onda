package dev.socialmusic.core.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.*
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.session.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import dev.socialmusic.domain.music.MusicSource
import dev.socialmusic.domain.music.PlaybackQueue
import dev.socialmusic.model.MusicId
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Owns the only player. All timeline mutation runs on its application looper. */
@AndroidEntryPoint
@UnstableApi
class PlaybackService : MediaSessionService() {
    @Inject lateinit var musicSource: MusicSource
    private lateinit var player: ExoPlayer
    private lateinit var resolver: StreamResolver
    private var session: MediaSession? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mutation = 0L
    private var restoring = true
    private val recovery = ExpiryRecovery()
    private lateinit var snapshot: File

    override fun onCreate() {
        super.onCreate()
        snapshot = File(filesDir, "playback-v1.json")
        resolver = StreamResolver(musicSource::getStream)
        val http = OkHttpDataSource.Factory(OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build())
        val data = ResolvingDataSource.Factory(http) { spec ->
            if (spec.uri.scheme != "onda" || spec.uri.host != "youtube") throw StreamResolutionException()
            try {
                val stream = runBlocking { resolver.resolve(MusicId("youtube", spec.uri.lastPathSegment.orEmpty())) }
                spec.withUri(android.net.Uri.parse(stream.url))
            } catch (cancelled: CancellationException) { throw IOException("Stream resolution cancelled", cancelled) }
              catch (_: Exception) { throw StreamResolutionException() }
        }
        val noRetry = object : DefaultLoadErrorHandlingPolicy(0) {
            override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long = C.TIME_UNSET
        }
        player = ExoPlayer.Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(data).setLoadErrorHandlingPolicy(noRetry)).build()
        player.setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
        player.setHandleAudioBecomingNoisy(true)
        player.setWakeMode(C.WAKE_MODE_LOCAL)
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) { persist() }
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) { persist() }
            override fun onPlayerError(error: PlaybackException) {
                val httpError = generateSequence<Throwable>(error) { it.cause }.filterIsInstance<HttpDataSource.InvalidResponseCodeException>().firstOrNull()
                val active = player.currentMediaItem
                if (httpError?.responseCode in listOf(403, 410) && active != null && recovery.allow(active.mediaId)) {
                    SessionProtocol.entry(active)?.let { resolver.invalidate(it.track.id) }
                    player.prepare()
                }
            }
        })
        val builder = MediaSession.Builder(this, player).setCallback(Callback())
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            builder.setSessionActivity(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
        session = builder.build()
        val initialMutation = mutation
        scope.launch {
            val restored = SnapshotStorage.read(snapshot)
            if (mutation == initialMutation && restored.queue.isNotEmpty()) {
                player.setMediaItems(restored.queue.map(SessionProtocol::item), restored.currentIndex, restored.positionMs)
                player.shuffleModeEnabled = restored.shuffle
                player.repeatMode = when (restored.repeat) { dev.socialmusic.model.RepeatMode.ONE -> Player.REPEAT_MODE_ONE; dev.socialmusic.model.RepeatMode.ALL -> Player.REPEAT_MODE_ALL; else -> Player.REPEAT_MODE_OFF }
                player.playWhenReady = false // Restoration never prepares or starts audio.
            }
            restoring = false
            persist()
        }
        scope.launch { while (isActive) { delay(5000); persist() } }
    }
    private fun persist() { if (!restoring) SnapshotStorage.write(snapshot, PlaybackSnapshotCodec.encode(SessionProtocol.state(player))) }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session
    override fun onDestroy() {
        resolver.invalidate()
        if (!restoring || mutation > 0) { restoring = false; persist() }
        scope.cancel()
        session?.release()
        session = null
        player.release()
        super.onDestroy()
    }

    private inner class Callback : MediaSession.Callback {
        override fun onConnect(session: MediaSession, controller: MediaSession.ControllerInfo): MediaSession.ConnectionResult {
            val own = controller.uid == Process.myUid()
            if (!own && !controller.isTrusted && !session.isMediaNotificationController(controller)) return MediaSession.ConnectionResult.reject()
            val commands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon().remove(Player.COMMAND_CHANGE_MEDIA_ITEMS).remove(Player.COMMAND_SET_MEDIA_ITEM).build()
            val sessions = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().apply { if (own) add(SessionProtocol.command) }.build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session).setAvailablePlayerCommands(commands).setAvailableSessionCommands(sessions).build()
        }
        override fun onPlayerCommandRequest(session: MediaSession, controller: MediaSession.ControllerInfo, playerCommand: Int): Int {
            mutation++ // Transport input also supersedes a late startup restore.
            return SessionResult.RESULT_SUCCESS
        }
        override fun onPlayerInteractionFinished(session: MediaSession, controller: MediaSession.ControllerInfo, playerCommands: Player.Commands) {
            val skipped = listOf(Player.COMMAND_SEEK_TO_NEXT, Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM, Player.COMMAND_SEEK_TO_PREVIOUS, Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM).any(playerCommands::contains)
            if (skipped && player.mediaItemCount > 0 && player.playbackState == Player.STATE_IDLE) player.prepare()
        }
        override fun onCustomCommand(session: MediaSession, controller: MediaSession.ControllerInfo, customCommand: SessionCommand, args: Bundle): ListenableFuture<SessionResult> {
            if (controller.uid != Process.myUid() || customCommand.customAction != SessionProtocol.ACTION) return Futures.immediateFuture(SessionResult(SessionError.ERROR_PERMISSION_DENIED))
            val result = runCatching { execute(args); SessionResult.RESULT_SUCCESS }.getOrDefault(SessionError.ERROR_BAD_VALUE)
            return Futures.immediateFuture(SessionResult(result))
        }
    }
    private fun execute(args: Bundle) {
        mutation++
        when (args.getString("operation")) {
            "play" -> {
                val tracks = SessionProtocol.tracks(args.getString("tracks"))
                require(tracks.isNotEmpty())
                val index = args.getInt("index")
                require(index in tracks.indices)
                resolver.invalidate(); recovery.reset()
                player.setMediaItems(PlaybackQueue.fromTracks(tracks).map(SessionProtocol::item), index, 0)
                player.prepare(); player.play()
            }
            "add", "next" -> {
                val tracks = SessionProtocol.tracks(args.getString("tracks"))
                require(tracks.size == 1 && player.mediaItemCount < PlaybackQueue.MAX_ENTRIES)
                val entry = PlaybackQueue.fromTracks(tracks).single()
                val index = if (args.getString("operation") == "next" && player.mediaItemCount > 0) player.currentMediaItemIndex + 1 else player.mediaItemCount
                player.addMediaItem(index, SessionProtocol.item(entry))
            }
            "remove" -> {
                val index = (0 until player.mediaItemCount).firstOrNull { player.getMediaItemAt(it).mediaId == args.getString("occurrence") } ?: return
                val recoverAfterRemoval = index == player.currentMediaItemIndex && player.playerError != null
                if (index == player.currentMediaItemIndex) resolver.invalidate()
                player.removeMediaItem(index)
                if (player.mediaItemCount == 0) player.stop()
                else if (recoverAfterRemoval) player.prepare()
            }
            "move" -> {
                val index = (0 until player.mediaItemCount).firstOrNull { player.getMediaItemAt(it).mediaId == args.getString("occurrence") } ?: return
                val target = args.getInt("index", -1)
                require(target in 0 until player.mediaItemCount)
                player.moveMediaItem(index, target)
            }
            "retry" -> {
                if (player.mediaItemCount > 0 && (player.playerError != null || player.playbackState == Player.STATE_IDLE)) {
                    resolver.invalidate(); recovery.reset(); player.prepare(); player.play()
                }
            }
            "clear" -> { resolver.invalidate(); recovery.reset(); player.stop(); player.clearMediaItems() }
            else -> error("Unknown queue operation")
        }
        persist()
    }
}
