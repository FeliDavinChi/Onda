package dev.socialmusic.domain.music

import dev.socialmusic.model.*
import java.util.UUID

/** Pure queue edits preserve occurrence identity rather than comparing provider IDs. */
object PlaybackQueue {
    const val MAX_ENTRIES = 200
    fun fromTracks(tracks: List<Track>): List<QueueEntry> = tracks.take(MAX_ENTRIES).map { QueueEntry(UUID.randomUUID().toString(), it) }

    fun move(state: PlaybackState, occurrenceId: String, toIndex: Int): PlaybackState {
        val from = state.queue.indexOfFirst { it.occurrenceId == occurrenceId }
        if (from < 0 || toIndex !in state.queue.indices || from == toIndex) return state
        val active = state.queue.getOrNull(state.currentIndex)?.occurrenceId
        val entries = state.queue.toMutableList().apply { add(toIndex, removeAt(from)) }
        return state.copy(queue = entries, currentIndex = entries.indexOfFirst { it.occurrenceId == active })
    }

    fun remove(state: PlaybackState, occurrenceId: String): PlaybackState {
        val index = state.queue.indexOfFirst { it.occurrenceId == occurrenceId }
        if (index < 0) return state
        val entries = state.queue.toMutableList().apply { removeAt(index) }
        if (entries.isEmpty()) return PlaybackState(shuffle = state.shuffle, repeat = state.repeat)
        val active = state.queue.getOrNull(state.currentIndex)?.occurrenceId
        val removedActive = active == occurrenceId
        val nextIndex = if (removedActive) index.coerceAtMost(entries.lastIndex) else entries.indexOfFirst { it.occurrenceId == active }
        return state.copy(queue = entries, currentIndex = nextIndex, positionMs = if (removedActive) 0 else state.positionMs, durationMs = if (removedActive) null else state.durationMs, error = if (removedActive) null else state.error)
    }
}
