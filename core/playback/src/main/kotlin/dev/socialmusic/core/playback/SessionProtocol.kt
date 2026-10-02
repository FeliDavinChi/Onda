package dev.socialmusic.core.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionCommand
import dev.socialmusic.model.*
import kotlinx.serialization.encodeToString

internal object SessionProtocol {
    const val ACTION = "dev.socialmusic.QUEUE_EDIT"
    val command get() = SessionCommand(ACTION, Bundle.EMPTY)
    fun payload(tracks: List<Track>): String = PlaybackSnapshotCodec.json.encodeToString(tracks.take(200).map(PlaybackSnapshotCodec::cleanTrack))
    fun tracks(encoded: String?): List<Track> {
        require(encoded != null && encoded.length <= 250_000)
        return PlaybackSnapshotCodec.json.decodeFromString<List<Track>>(encoded).also { tracks -> require(tracks.size <= 200 && tracks.all(PlaybackSnapshotCodec::validTrack)) }.map(PlaybackSnapshotCodec::cleanTrack)
    }
    fun item(entry: QueueEntry): MediaItem = MediaItem.Builder().setMediaId(entry.occurrenceId)
        .setUri("onda://youtube/${entry.track.id.providerId}")
        .setMediaMetadata(MediaMetadata.Builder().setTitle(entry.track.title).setArtist(entry.track.artists.joinToString { it.name })
            .setArtworkUri(entry.track.artwork?.let(Uri::parse))
            .setExtras(Bundle().apply { putString("track", PlaybackSnapshotCodec.json.encodeToString(PlaybackSnapshotCodec.cleanTrack(entry.track))) }).build()).build()
    fun entry(item: MediaItem): QueueEntry? = runCatching {
        val encoded = item.mediaMetadata.extras?.getString("track") ?: return null
        val track = PlaybackSnapshotCodec.json.decodeFromString<Track>(encoded)
        require(PlaybackSnapshotCodec.validTrack(track))
        QueueEntry(item.mediaId, track)
    }.getOrNull()
    @UnstableApi fun state(player: Player): PlaybackState {
        val queue = (0 until player.mediaItemCount).mapNotNull { entry(player.getMediaItemAt(it)) }
        val index = queue.indexOfFirst { it.occurrenceId == player.currentMediaItem?.mediaId }
        val status = when {
            queue.isEmpty() -> PlaybackStatus.IDLE
            player.playerError != null -> PlaybackStatus.ERROR
            player.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
            player.isPlaying -> PlaybackStatus.PLAYING
            player.playbackState == Player.STATE_ENDED -> PlaybackStatus.ENDED
            else -> PlaybackStatus.PAUSED
        }
        return PlaybackState(queue, index, status, buffering = player.playbackState == Player.STATE_BUFFERING,
            positionMs = player.currentPosition.coerceAtLeast(0), durationMs = player.duration.takeIf { it != C.TIME_UNSET && it >= 0 },
            shuffle = player.shuffleModeEnabled, repeat = when (player.repeatMode) { Player.REPEAT_MODE_ONE -> RepeatMode.ONE; Player.REPEAT_MODE_ALL -> RepeatMode.ALL; else -> RepeatMode.OFF },
            error = player.playerError?.let { PlaybackError(it.errorCodeName, true) }, playWhenReady = player.playWhenReady)
    }
}
