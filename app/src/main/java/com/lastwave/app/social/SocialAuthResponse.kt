package com.lastwave.app.social

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal fun signupHasSession(body: String): Boolean {
    val response = authResponse(body) ?: error("Onda received an incomplete sign-up response. Try again.")
    val user = response["user"] as? JsonObject
    if (response.nonBlankString("access_token") != null) {
        check(response.nonBlankString("refresh_token") != null && user?.nonBlankString("id") != null) {
            "Onda received an incomplete sign-up response. Try again."
        }
        return true
    }
    check(response.nonBlankString("id") != null || user?.nonBlankString("id") != null) {
        "Onda received an incomplete sign-up response. Try again."
    }
    return false
}

internal fun socialAuthErrorMessage(status: Int, body: String): String = when (authResponse(body)?.nonBlankString("error_code")) {
    "email_address_not_authorized" -> "Onda can't send a confirmation email to this address right now."
    "email_not_confirmed" -> "Confirm your email before signing in. You can request another confirmation email below."
    "over_email_send_rate_limit", "over_request_rate_limit" -> "Too many account requests. Wait a while and try again."
    "signup_disabled", "email_provider_disabled" -> "Onda account sign-up is temporarily unavailable."
    "email_exists", "user_already_exists" -> "Try signing in or request a new confirmation email below."
    "invalid_credentials" -> "Check your email and password and try again."
    "email_address_invalid" -> "Enter a valid email address."
    "weak_password" -> "Choose a stronger password for your Onda account."
    else -> when {
        status == 429 -> "Too many account requests. Wait a while and try again."
        status == 401 -> "Onda could not authenticate this request. Try signing in again."
        else -> "Onda could not complete your account request. Please try again."
    }
}

private fun authResponse(body: String): JsonObject? = runCatching { Json.parseToJsonElement(body) as? JsonObject }.getOrNull()

private fun JsonObject.nonBlankString(key: String): String? =
    (get(key) as? JsonPrimitive)?.takeIf { it.isString && it.content.isNotBlank() }?.content
