package dev.socialmusic.model

import org.junit.Assert.*
import org.junit.Test

class MusicIdTest {
    @Test fun sameProviderIdCannotCollideAcrossProviders() {
        assertNotEquals(MusicId("demo", "one").canonical, MusicId("youtube", "one").canonical)
    }
    @Test fun canonicalIdentityIsStableAndEscapesSeparators() {
        assertEquals("demo:one%3Atwo", MusicId("demo", "one:two").canonical)
        assertEquals("demo:a%2Fb%25c%20d", MusicId("demo", "a/b%c d").canonical)
    }
    @Test fun rejectsBlankId() {
        assertThrows(IllegalArgumentException::class.java) { MusicId("demo", "") }
    }
    @Test fun providerUsesRestrictedNamespace() {
        assertThrows(IllegalArgumentException::class.java) { MusicId("bad:provider", "one") }
    }
    @Test fun malformedUnicodeCannotCreateIdentityCollisions() {
        assertThrows(IllegalArgumentException::class.java) { MusicId("demo", "\uD800") }
        assertThrows(IllegalArgumentException::class.java) { MusicId("demo", "\uDC00") }
        assertNotEquals(MusicId("demo", "\uD83C\uDFB5").canonical, MusicId("demo", "?").canonical)
    }
}
