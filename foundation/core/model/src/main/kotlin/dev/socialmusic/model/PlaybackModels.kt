package dev.socialmusic.model

enum class PlaybackStatus { IDLE, BUFFERING, PLAYING, PAUSED, ENDED, ERROR }
enum class RepeatMode { OFF, ONE, ALL }
data class QueueEntry(val occurrenceId: String, val track: Track)
data class PlaybackError(val code: String, val recoverable: Boolean)

data class PlaybackState(
    val queue: List<QueueEntry> = emptyList(),
    val currentIndex: Int = -1,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val buffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long? = null,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.OFF,
    val error: PlaybackError? = null,
) {
    init {
        require(currentIndex == -1 || currentIndex in queue.indices)
        require(positionMs >= 0)
        require(durationMs == null || durationMs >= 0)
        require(queue.all { it.occurrenceId.isNotBlank() })
        require(queue.map { it.occurrenceId }.distinct().size == queue.size)
    }
    val currentTrack: Track? get() = queue.getOrNull(currentIndex)?.track
}
