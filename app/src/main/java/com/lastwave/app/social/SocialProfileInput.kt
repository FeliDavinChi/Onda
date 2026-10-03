package com.lastwave.app.social

import java.util.Locale

/** Profile validation shared by the form's save action and field feedback. */
internal class SocialProfileInput(rawUsername: String, rawDisplayName: String) {
    val username = rawUsername.trim().lowercase(Locale.ROOT)
    val displayName = rawDisplayName.trim()
    val usernameError: String? = when {
        username.length !in 3..24 -> "Use 3–24 letters, numbers, or underscores."
        !Regex("^[a-z0-9_]+$").matches(username) -> "Use only letters, numbers, or underscores."
        else -> null
    }
    val displayNameError: String? = when {
        displayName.isEmpty() -> "Enter your display name."
        displayName.codePointCount(0, displayName.length) > 80 -> "Use a display name of 80 characters or fewer."
        else -> null
    }
    val canSave = usernameError == null && displayNameError == null
}
