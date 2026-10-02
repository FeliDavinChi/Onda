package dev.socialmusic.domain.music

import dev.socialmusic.model.PlaybackState
import dev.socialmusic.model.RepeatMode
import dev.socialmusic.model.Track
import kotlinx.coroutines.flow.StateFlow

/** Commands are asynchronous; state reflects the service's authoritative player timeline. */
interface PlaybackController {
    val state: StateFlow<PlaybackState>
    fun play(tracks: List<Track>, startIndex: Int = 0)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun next()
    fun previous()
    fun setShuffle(enabled: Boolean)
    fun setRepeat(mode: RepeatMode)
    fun addToQueue(track: Track)
    fun playNext(track: Track)
    fun remove(occurrenceId: String)
    fun move(occurrenceId: String, toIndex: Int)
    fun retry()
    fun clear()
}
