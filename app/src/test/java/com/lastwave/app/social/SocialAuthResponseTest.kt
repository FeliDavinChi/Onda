package com.lastwave.app.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SocialAuthResponseTest {
    @Test fun signupWithoutASessionCanRequireEmailConfirmation() {
        assertFalse(signupHasSession("""{"id":"user-id","email":"friend@example.com"}"""))
        assertFalse(signupHasSession("""{"access_token":null,"user":{"id":"user-id"}}"""))
    }

    @Test fun signupWithAnAuthenticatedSessionCanSignInImmediately() {
        assertTrue(signupHasSession("""{"access_token":"access","refresh_token":"refresh","user":{"id":"user-id"}}"""))
    }

    @Test fun incompleteSignupResponsesDoNotClaimAnEmailWasSent() {
        for (response in listOf("{}", "[]", "<html>", """{"access_token":"access","user":{"id":"user-id"}}""")) {
            assertThrows(IllegalStateException::class.java) { signupHasSession(response) }
        }
    }

    @Test fun emailDeliveryRestrictionIsExplainedWithoutExposingTheAddress() {
        val body = """{"error_code":"email_address_not_authorized","msg":"Email address friend@example.com is not authorized"}"""
        val message = socialAuthErrorMessage(403, body)
        assertEquals("Onda can't send a confirmation email to this address right now.", message)
        assertFalse(message.contains("friend@example.com"))
    }

    @Test fun unconfirmedAndRateLimitedAccountsGetActionableMessages() {
        assertEquals(
            "Confirm your email before signing in. You can request another confirmation email below.",
            socialAuthErrorMessage(400, """{"error_code":"email_not_confirmed"}"""),
        )
        assertEquals("Too many account requests. Wait a while and try again.", socialAuthErrorMessage(429, ""))
    }
}
