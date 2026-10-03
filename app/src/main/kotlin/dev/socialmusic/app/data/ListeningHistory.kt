package dev.socialmusic.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val Context.listeningPreferences by preferencesDataStore(name = "listening")
private val recentKey = stringPreferencesKey("recent")
private const val MAX_BYTES = 128 * 1024
private val historyJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class HistorySnapshot(val version: Int = 1, val entries: List<Track> = emptyList())

/** Private, bounded continuity data. Provider stream URLs and arbitrary metadata are never stored. */
@Singleton
class ListeningHistory internal constructor(private val store: DataStore<Preferences>) {
    @Inject constructor(@ApplicationContext context: Context) : this(context.listeningPreferences)

    val recent: Flow<List<Track>> = store.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { decode(it[recentKey]) }

    suspend fun record(track: Track) {
        val clean = track.copy(title = track.title.take(512), artists = track.artists.take(4).map { it.copy(name = it.name.take(256)) },
            album = null, artwork = track.artwork?.takeIf { it.length <= 2048 }, metadata = emptyMap())
        store.edit { preferences ->
            var entries = (listOf(clean) + decode(preferences[recentKey]).filter { it.id != clean.id }).take(20)
            var encoded = historyJson.encodeToString(HistorySnapshot.serializer(), HistorySnapshot(entries = entries))
            while (encoded.toByteArray(Charsets.UTF_8).size > MAX_BYTES && entries.isNotEmpty()) {
                entries = entries.dropLast(1)
                encoded = historyJson.encodeToString(HistorySnapshot.serializer(), HistorySnapshot(entries = entries))
            }
            preferences[recentKey] = encoded
        }
    }
    suspend fun clear() { store.edit { it.remove(recentKey) } }

    private fun decode(raw: String?): List<Track> {
        if (raw == null || raw.length > MAX_BYTES || raw.toByteArray(Charsets.UTF_8).size > MAX_BYTES) return emptyList()
        return runCatching { historyJson.decodeFromString(HistorySnapshot.serializer(), raw) }
            .getOrNull()?.takeIf { it.version == 1 }?.entries?.take(20)?.distinctBy { it.id }
            ?.map { it.copy(metadata = emptyMap()) } ?: emptyList()
    }
}

/** Process lifetime observer, independent of the activity and the service-owned audio player. */
@Singleton
class ListeningRecorder @Inject constructor(private val player: PlaybackController, private val history: ListeningHistory) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    fun start() { if (job == null) job = scope.launchRecordings(player.state, history::record) }
}

internal fun CoroutineScope.launchRecordings(states: Flow<PlaybackState>, record: suspend (Track) -> Unit): Job = launch {
    states.map { if (it.status == PlaybackStatus.PLAYING && !it.buffering) it.queue.getOrNull(it.currentIndex) else null }
        .distinctUntilChangedBy { it?.let { entry -> entry.occurrenceId to entry.track.id } }
        .filterNotNull().collect { entry ->
            try { record(entry.track) } catch (_: IOException) { /* Disk failures must not interrupt playback or later observation. */ }
        }
}
