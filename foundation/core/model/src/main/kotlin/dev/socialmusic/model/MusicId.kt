package dev.socialmusic.model

import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Serializable
data class MusicId(val provider: String, val providerId: String) {
    init {
        require(provider.matches(Regex("[a-z][a-z0-9_-]{0,31}"))) { "Invalid music provider namespace" }
        require(providerId.isNotBlank() && providerId.length <= 512) { "Invalid provider ID" }
        require(StandardCharsets.UTF_8.newEncoder().canEncode(providerId)) { "Provider ID contains malformed Unicode" }
    }
    val canonical: String
        get() = provider + ":" + URLEncoder.encode(providerId, StandardCharsets.UTF_8.name()).replace("+", "%20")
}
