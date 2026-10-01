package dev.socialmusic.common

import java.net.URI

/** Configuration helps prevent accidental disclosure; server RLS remains the security boundary. */
class BackendConfig private constructor(val url: String, val publishableKey: String) {
    override fun toString() = "BackendConfig(url=$url, publishableKey=[redacted])"
    companion object {
        fun from(url: String?, key: String?): BackendConfig? {
            val normalizedUrl = url?.trim().orEmpty()
            val normalizedKey = key?.trim().orEmpty()
            if (normalizedUrl.isEmpty() && normalizedKey.isEmpty()) return null
            require(normalizedUrl.isNotEmpty() && normalizedKey.isNotEmpty()) { "Backend configuration is incomplete" }
            val uri = try { URI(normalizedUrl) } catch (_: Exception) {
                throw IllegalArgumentException("Backend project URL is invalid")
            }
            require(uri.scheme == "https" && !uri.host.isNullOrBlank()) { "Backend requires an HTTPS project URL" }
            require(uri.userInfo == null && uri.query == null && uri.fragment == null) { "Backend URL must not contain credentials, query or fragment" }
            require(uri.path.isNullOrEmpty() || uri.path == "/") { "Backend URL must be the project root" }
            require(normalizedKey.matches(Regex("sb_publishable_[A-Za-z0-9_-]+"))) { "Only public publishable keys are accepted" }
            return BackendConfig(normalizedUrl.removeSuffix("/"), normalizedKey)
        }
    }
}
