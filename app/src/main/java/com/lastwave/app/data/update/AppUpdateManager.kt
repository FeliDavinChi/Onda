package com.lastwave.app.data.update

import android.content.Context
import com.lastwave.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

data class UpdateInfo(
    val isChecking: Boolean = false, val isUpdateAvailable: Boolean = false,
    val latestVersion: String = "", val currentVersion: String = "",
    val releaseNotes: String = "", val releaseUrl: String = "",
    val downloadUrl: String? = null, val isDismissed: Boolean = false,
    val message: String? = null,
)

/** Onda has no APK update channel yet. An upstream APK must never replace it. */
@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Suppress("UNUSED_PARAMETER") okHttpClient: OkHttpClient,
) {
    private val _updateInfo = MutableStateFlow(UpdateInfo(currentVersion = BuildConfig.VERSION_NAME))
    val updateInfo: StateFlow<UpdateInfo> = _updateInfo.asStateFlow()
    fun getCurrentVersion(): String = BuildConfig.VERSION_NAME
    fun checkForUpdate(isSilent: Boolean = false) {
        _updateInfo.update { UpdateInfo(currentVersion = getCurrentVersion(), message =
            if (isSilent) null else "Onda updates are not available in this development build.") }
    }
    fun dismissUpdate(@Suppress("UNUSED_PARAMETER") version: String) { _updateInfo.update { it.copy(isDismissed = true) } }
    fun openUpdate(@Suppress("UNUSED_PARAMETER") context: Context) = Unit

    fun isNewerVersion(remote: String, local: String): Boolean {
        if (remote.isBlank() || local.isBlank()) return false
        val cleanRemote = remote.removePrefix("v").removePrefix("V").substringBefore("-")
        val cleanLocal = local.removePrefix("v").removePrefix("V").substringBefore("-")
        if (cleanRemote == cleanLocal) return false

        val rParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = cleanLocal.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
