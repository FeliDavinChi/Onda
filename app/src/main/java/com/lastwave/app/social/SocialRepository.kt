package com.lastwave.app.social

import android.content.Context
import com.lastwave.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepository @Inject constructor(@ApplicationContext context: Context) {
    private val baseUrl = BuildConfig.ONDA_SUPABASE_URL.trimEnd('/')
    private val publicKey = BuildConfig.ONDA_SUPABASE_PUBLISHABLE_KEY
    val configured = baseUrl.startsWith("https://") && publicKey.startsWith("sb_publishable_")
    private val client = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
    private val store = SocialSessionStore(context)
    private var session: JSONObject? = store.read()?.let { runCatching { JSONObject(it) }.getOrNull() }
    private val refreshMutex = Mutex()
    private val _account = MutableStateFlow(session?.account())
    val account = _account.asStateFlow()
    private val _preferences = MutableStateFlow(SocialPreferences())
    val preferences = _preferences.asStateFlow()

    private fun JSONObject.account(): OndaAccount? = optJSONObject("user")?.let {
        val id = it.optString("id"); if (id.isBlank()) null else OndaAccount(id, it.optString("email"))
    }
    private suspend fun request(path: String, body: JSONObject, token: String? = null): String = withContext(Dispatchers.IO) {
        check(configured) { "Accounts are not available in this build yet" }
        val builder = Request.Builder().url("$baseUrl$path").header("apikey", publicKey)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
        if (token != null) builder.header("Authorization", "Bearer $token")
        suspendCancellableCoroutine { continuation ->
            val call = client.newCall(builder.build())
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, error: java.io.IOException) {
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val result = runCatching { response.use {
                        val value = it.body?.string().orEmpty()
                        if (!it.isSuccessful) {
                            val error = runCatching { JSONObject(value).optString("msg").ifBlank { JSONObject(value).optString("message").ifBlank { JSONObject(value).optString("error") } } }.getOrDefault("")
                            throw IllegalStateException(error.ifBlank { if (it.code == 401) "Your session expired. Sign in again." else "Could not connect. Please try again." }.take(250))
                        }
                        value
                    } }
                    if (continuation.isActive) result.fold(continuation::resume, continuation::resumeWithException)
                }
            })
        }
    }
    private suspend fun saveSession(value: JSONObject) = withContext(Dispatchers.IO) {
        require(!value.optString("access_token").isBlank() && value.account() != null) { "Account session is unavailable" }
        value.put("expires_at_ms", System.currentTimeMillis() + value.optLong("expires_in", 3600) * 1000)
        store.write(value.toString()); session = value; _account.value = value.account()
    }
    private suspend fun token(owner: String): String = refreshMutex.withLock {
        val current = session ?: error("Sign in to Onda first")
        if (current.account()?.id != owner) throw kotlinx.coroutines.CancellationException("Account changed")
        if (current.optLong("expires_at_ms") <= System.currentTimeMillis() + 60000) {
            val refreshed = JSONObject(request("/auth/v1/token?grant_type=refresh_token", JSONObject().put("refresh_token", current.getString("refresh_token"))))
            check(refreshed.account()?.id == current.account()?.id) { "Account session changed. Sign in again." }
            saveSession(refreshed)
        }
        session!!.getString("access_token")
    }
    suspend fun signIn(email: String, password: String) = refreshMutex.withLock {
        saveSession(JSONObject(request("/auth/v1/token?grant_type=password", JSONObject().put("email", email.trim()).put("password", password))))
    }
    suspend fun signUp(email: String, password: String): Boolean = refreshMutex.withLock {
        require(password.length >= 8) { "Use a password with at least 8 characters" }
        val result = JSONObject(request("/auth/v1/signup", JSONObject().put("email", email.trim()).put("password", password)))
        if (result.optString("access_token").isNotBlank()) { saveSession(result); true }
        else false // Email verification is required; no pretend signed-in state.
    }
    suspend fun signOut() = withContext(NonCancellable) {
        refreshMutex.withLock {
            val old = session?.optString("access_token")
            withContext(Dispatchers.IO) { store.clear() }
            session = null; _preferences.value = SocialPreferences(); _account.value = null
            // The account observer cancels old work. Revocation still completes,
            // bounded by the HTTP client's 20-second timeout.
            if (!old.isNullOrBlank()) runCatching { request("/auth/v1/logout", JSONObject(), old) }
        }
    }
    private suspend fun rpc(name: String, body: JSONObject = JSONObject()): String {
        val owner = _account.value?.id ?: error("Sign in to Onda first")
        val result = request("/rest/v1/rpc/$name", body, token(owner))
        if (_account.value?.id != owner) throw kotlinx.coroutines.CancellationException("Account changed")
        return result
    }
    suspend fun setupProfile(username: String, displayName: String) {
        rpc("setup_profile", JSONObject().put("p_username", username.trim().lowercase()).put("p_display_name", displayName.trim()))
    }
    private fun JSONObject.person(): SocialPerson {
        val listening = optJSONObject("listening")?.let { activity ->
            val id = activity.optString("video_id")
            if (!YouTubeIdentity.matches(id)) null else SharedListening(id, activity.optString("title"), activity.optString("artist"), activity.optBoolean("live"))
        }
        return SocialPerson(getString("id"), getString("username"), getString("display_name"), optBoolean("following"),
            optBoolean("influence_enabled"), optBoolean("can_influence"), optBoolean("hidden_activity"), listening)
    }
    suspend fun home(): SocialHome {
        val owner = _account.value?.id ?: error("Sign in to Onda first")
        val value = JSONObject(rpc("social_home"))
        val settings = value.optJSONObject("settings")
        val blocked = JSONArray(rpc("blocked_people"))
        if (_account.value?.id != owner) throw kotlinx.coroutines.CancellationException("Account changed")
        val preferences = SocialPreferences(settings?.optBoolean("listening_shared") ?: false, settings?.optBoolean("taste_shared") ?: false, settings?.optBoolean("private_session") ?: false, settings?.optBoolean("personalization_enabled") ?: false)
        _preferences.value = preferences
        val people = value.optJSONArray("following") ?: JSONArray()
        return SocialHome(value.optJSONObject("profile")?.person(), preferences, (0 until people.length()).map { people.getJSONObject(it).person() }, (0 until blocked.length()).map { blocked.getJSONObject(it).person() })
    }
    suspend fun search(query: String): List<SocialPerson> {
        val values = JSONArray(rpc("search_people", JSONObject().put("p_query", query)))
        return (0 until values.length()).map { values.getJSONObject(it).person() }
    }
    suspend fun follow(id: String, enabled: Boolean) { rpc("set_follow", JSONObject().put("p_user_id", id).put("p_following", enabled)) }
    suspend fun influence(id: String, enabled: Boolean) { rpc("set_influence", JSONObject().put("p_user_id", id).put("p_enabled", enabled)) }
    suspend fun block(id: String, enabled: Boolean) { rpc("set_block", JSONObject().put("p_user_id", id).put("p_blocked", enabled)) }
    suspend fun hide(id: String, hidden: Boolean) { rpc("hide_listening", JSONObject().put("p_user_id", id).put("p_hidden", hidden)) }
    suspend fun setPreferences(value: SocialPreferences) {
        rpc("set_social_preferences", JSONObject().put("p_listening_shared", value.listeningShared).put("p_taste_shared", value.tasteShared).put("p_private_session", value.privateSession).put("p_personalization_enabled", value.personalizationEnabled))
        _preferences.value = value
    }
    suspend fun publish(track: com.lastwave.app.playback.PlayableTrack, playing: Boolean) {
        val id = track.videoId ?: return
        if (!YouTubeIdentity.matches(id)) return
        rpc("publish_listening", JSONObject().put("p_video_id", id).put("p_title", track.title.take(300)).put("p_artist", track.artist.take(300)).put("p_is_playing", playing))
    }
    suspend fun recommendations(): List<SocialRecommendation> {
        val owner = _account.value?.id ?: error("Sign in to Onda first")
        val value = JSONObject(request("/functions/v1/recommendations", JSONObject(), token(owner)))
        if (_account.value?.id != owner) throw kotlinx.coroutines.CancellationException("Account changed")
        check(value.optInt("schema_version") == 1) { "Update Onda to load these recommendations" }
        val items = value.optJSONArray("items") ?: JSONArray()
        return (0 until items.length()).mapNotNull { index -> items.getJSONObject(index).let {
            val videoId = it.optString("video_id")
            if (!YouTubeIdentity.matches(videoId)) null else SocialRecommendation(it.getString("id"), videoId, it.getString("title"), it.getString("artist"), it.getString("reason"))
        } }
    }
    suspend fun recordEvent(track: com.lastwave.app.playback.PlayableTrack, kind: String, eventId: String = java.util.UUID.randomUUID().toString()) {
        val id = track.videoId ?: return
        if (!YouTubeIdentity.matches(id)) return
        rpc("record_listening_event", JSONObject().put("p_id", eventId).put("p_video_id", id).put("p_title", track.title.take(300)).put("p_artist", track.artist.take(300)).put("p_kind", kind))
    }
}
