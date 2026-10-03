package com.lastwave.app.social

import android.content.Context
import com.lastwave.app.data.update.AppUpdateManager
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.Assert.*

class OndaUpdateIsolationTest {
    @Test fun anOndaBuildCannotOpenAnUpstreamApk() {
        val context = mockk<Context>(relaxed = true)
        val client = mockk<OkHttpClient>(relaxed = true)
        val updates = AppUpdateManager(context, client)
        updates.checkForUpdate()
        updates.openUpdate(context)
        verify(exactly = 0) { client.newCall(any()) }
        verify(exactly = 0) { context.startActivity(any()) }
        assertFalse(updates.updateInfo.value.isUpdateAvailable)
        assertTrue(updates.updateInfo.value.releaseUrl.isEmpty())
    }
}
