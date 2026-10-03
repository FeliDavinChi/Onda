package dev.socialmusic.core.playback

import dev.socialmusic.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PlaybackSnapshotCodec {
    internal val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    @Serializable private data class Entry(val occurrenceId: String, val track: Track)
    @Serializable private data class Snapshot(val version: Int = 1, val queue: List<Entry>, val currentOccurrence: String?, val positionMs: Long, val shuffle: Boolean, val repeat: RepeatMode)
    fun encode(state: PlaybackState): String = json.encodeToString(Snapshot(queue = state.queue.take(200).map { Entry(it.occurrenceId, cleanTrack(it.track)) }, currentOccurrence = state.queue.getOrNull(state.currentIndex)?.occurrenceId, positionMs = state.positionMs, shuffle = state.shuffle, repeat = state.repeat))
    fun decode(encoded: String?): PlaybackState {
        if (encoded == null || encoded.length > 1_000_000) return PlaybackState()
        return runCatching {
            val snapshot = json.decodeFromString<Snapshot>(encoded)
            require(snapshot.version == 1 && snapshot.queue.size <= 200)
            val queue = snapshot.queue.map { require(validTrack(it.track)); QueueEntry(it.occurrenceId, cleanTrack(it.track)) }
            if (queue.isEmpty()) PlaybackState(shuffle = snapshot.shuffle, repeat = snapshot.repeat)
            else PlaybackState(queue, queue.indexOfFirst { it.occurrenceId == snapshot.currentOccurrence }.coerceAtLeast(0), PlaybackStatus.PAUSED, positionMs = snapshot.positionMs.coerceAtLeast(0), shuffle = snapshot.shuffle, repeat = snapshot.repeat)
        }.getOrDefault(PlaybackState())
    }
    internal fun validTrack(track: Track) = track.id.provider == "youtube" && Regex("[a-zA-Z0-9_-]{11}").matches(track.id.providerId) && track.title.length <= 1000 && track.artists.size <= 20
    internal fun cleanTrack(track: Track) = track.copy(metadata = emptyMap(), artwork = track.artwork?.takeIf { runCatching { java.net.URI(it).let { uri -> uri.scheme == "https" && uri.host != null && uri.userInfo == null } }.getOrDefault(false) })
}
