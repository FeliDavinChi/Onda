package com.lastwave.app.social

import java.util.Locale
import org.junit.Assert.*
import org.junit.Test

class SocialProfileInputTest {
    @Test fun mixedCaseUsernameFromProfileSetupCanBeSaved() {
        val input = SocialProfileInput("FeliDavinChi", "Manas Gupta")
        assertTrue("A valid mixed-case username must not disable Save profile", input.canSave)
        assertEquals("felidavinchi", input.username)
        assertNull(input.usernameError)
    }

    @Test fun pastedUsernameAndDisplayNameAreTrimmedBeforeValidation() {
        val input = SocialProfileInput("  Friend_123  ", "  Friend  ")
        assertTrue(input.canSave)
        assertEquals("friend_123", input.username)
        assertEquals("Friend", input.displayName)
    }

    @Test fun invalidUsernameExplainsWhyProfileCannotBeSaved() {
        for (username in listOf("ab", "a".repeat(25), "friend name", "friend.name", "friend@name", "friend😀")) {
            val input = SocialProfileInput(username, "Friend")
            assertFalse(username, input.canSave)
            assertNotNull(username, input.usernameError)
        }
        assertFalse(SocialProfileInput("", "Friend").canSave)
    }

    @Test fun displayNameMustFitTheDatabaseProfileLimit() {
        assertFalse(SocialProfileInput("friend", "  ").canSave)
        assertFalse(SocialProfileInput("friend", "a".repeat(81)).canSave)
        assertTrue(SocialProfileInput("a_1", "a".repeat(80)).canSave)
        assertTrue(SocialProfileInput("a".repeat(24), "Friend").canSave)
    }

    @Test fun usernameNormalizationDoesNotDependOnPhoneLocale() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val input = SocialProfileInput("INDIE_FAN", "Friend")
            assertTrue(input.canSave)
            assertEquals("indie_fan", input.username)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}
