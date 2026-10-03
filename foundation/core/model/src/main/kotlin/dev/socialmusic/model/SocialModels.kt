package dev.socialmusic.model

enum class Visibility { EVERYONE, FOLLOWERS, MUTUALS, NOBODY }
enum class SyncStatus { PENDING, SYNCED, FAILED }

data class UserProfile(
    val id: String,
    val username: String,
    val displayName: String,
    val avatar: String? = null,
    val bio: String = "",
)

data class PrivacySettings(
    val profile: Visibility = Visibility.EVERYONE,
    val nowPlaying: Visibility = Visibility.NOBODY,
    val history: Visibility = Visibility.NOBODY,
    val activity: Visibility = Visibility.NOBODY,
    val presence: Visibility = Visibility.NOBODY,
    val socialRecommendationsOptIn: Boolean = false,
)

data class Conversation(val id: String, val memberIds: Set<String>, val updatedAtEpochMs: Long)
enum class MusicEntityKind { TRACK, ALBUM, ARTIST, PLAYLIST }
sealed interface MessageContent {
    data class Text(val text: String) : MessageContent
    data class MusicShare(val kind: MusicEntityKind, val musicId: MusicId, val titleSnapshot: String) : MessageContent
}
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: MessageContent,
    val createdAtEpochMs: Long,
    val replyToMessageId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Long = 0,
)
data class ListeningActivity(val id: String, val userId: String, val trackId: MusicId, val startedAtEpochMs: Long)
