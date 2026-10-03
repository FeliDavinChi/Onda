package com.lastwave.app.social

data class OndaAccount(val id: String, val email: String)
// Fail closed until the server settings are loaded. New-profile defaults live in
// the database; this placeholder must not publish before an existing user's choices arrive.
data class SocialPreferences(val listeningShared: Boolean = false, val tasteShared: Boolean = false, val privateSession: Boolean = false, val personalizationEnabled: Boolean = false)
data class SharedListening(val videoId: String, val title: String, val artist: String, val live: Boolean)
data class SocialPerson(
    val id: String, val username: String, val displayName: String,
    val following: Boolean = false, val influenceEnabled: Boolean = false,
    val canInfluence: Boolean = false, val hiddenActivity: Boolean = false,
    val listening: SharedListening? = null,
)
data class SocialHome(val profile: SocialPerson? = null, val preferences: SocialPreferences = SocialPreferences(), val following: List<SocialPerson> = emptyList(), val blockedPeople: List<SocialPerson> = emptyList())
data class SocialRecommendation(val id: String, val videoId: String, val title: String, val artist: String, val reason: String)

internal val YouTubeIdentity = Regex("^[A-Za-z0-9_-]{11}$")

internal fun sharedTrack(videoId: String, title: String, artist: String): com.lastwave.app.playback.PlayableTrack {
    require(YouTubeIdentity.matches(videoId)) { "This song cannot be played from Onda yet" }
    require(title.isNotBlank() && artist.isNotBlank()) { "Song details are unavailable" }
    // Never accept serialized streams, local paths or cookies from another user.
    return com.lastwave.app.playback.PlayableTrack(title = title, artist = artist, videoId = videoId)
}
