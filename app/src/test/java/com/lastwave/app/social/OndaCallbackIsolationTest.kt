package com.lastwave.app.social

import android.content.Intent
import android.net.Uri
import com.lastwave.app.data.repository.LastFmAuthCallbackCoordinator
import com.lastwave.app.data.repository.LAST_FM_AUTH_CALLBACK_URI
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class OndaCallbackIsolationTest {
    @Test fun authenticationAcceptsOnlyTheOndaCallback() {
        val uri = mockk<Uri>()
        val intent = mockk<Intent>()
        every { intent.data } returns uri
        every { uri.host } returns "auth-callback"
        every { uri.getQueryParameter("token") } returns "test-auth-code"
        val coordinator = LastFmAuthCallbackCoordinator()
        every { uri.scheme } returns "lastwave"
        assertFalse(coordinator.capture(intent))
        every { uri.scheme } returns "onda"
        assertTrue(coordinator.capture(intent))
        assertEquals("test-auth-code", coordinator.pendingToken.value)
        assertEquals("onda://auth-callback", LAST_FM_AUTH_CALLBACK_URI)
    }
}
