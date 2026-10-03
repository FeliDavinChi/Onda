package dev.socialmusic.common

import org.junit.Assert.*
import org.junit.Test

class BackendConfigTest {
    @Test fun unconfiguredBackendIsAbsent() {
        assertNull(BackendConfig.from(null, null))
        assertNull(BackendConfig.from("", ""))
    }
    @Test fun acceptsPublicKeyAndHttpsProjectUrl() {
        val config = BackendConfig.from(" https://project.supabase.co/ ", "sb_publishable_example")
        assertEquals("https://project.supabase.co", config?.url)
        assertEquals("sb_publishable_example", config?.publishableKey)
    }
    @Test fun rejectsPartialConfiguration() { rejected("https://project.supabase.co", "") }
    @Test fun rejectsPlaintext() { rejected("http://project.supabase.co", "sb_publishable_example") }
    @Test fun rejectsSecretKey() { rejected("https://project.supabase.co", "sb_secret_example") }
    @Test fun rejectsUnknownKeyFormat() { rejected("https://project.supabase.co", "arbitrary-token") }
    @Test fun rejectsCredentialsInUrl() { rejected("https://user:password@project.supabase.co", "sb_publishable_example") }
    @Test fun rejectsQueryAndFragmentInProjectUrl() {
        rejected("https://project.supabase.co?token=secret", "sb_publishable_example")
        rejected("https://project.supabase.co#fragment", "sb_publishable_example")
    }
    @Test fun logsNeverIncludePublicCredentialMaterial() {
        val config = BackendConfig.from("https://project.supabase.co", "sb_publishable_example")
        assertNotNull(config)
        assertFalse(config.toString().contains("sb_publishable_example"))
    }
    private fun rejected(url: String, key: String) {
        assertThrows(IllegalArgumentException::class.java) { BackendConfig.from(url, key) }
    }
}
