package dev.socialmusic.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import dev.socialmusic.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListeningHistoryTest {
    private fun track(index: Int) = Track(MusicId("fixture", "$index"), "Track $index", emptyList(), metadata = mapOf("streamUrl" to "private"))

    @Test fun recentIsBoundedUniqueAndNewestFirst() = runTest {
        val history = ListeningHistory(MemoryPreferences())
        repeat(25) { history.record(track(it)) }
        history.record(track(12))
        val recent = history.recent.first()
        assertEquals(20, recent.size)
        assertEquals(track(12).id, recent.first().id)
        assertEquals(20, recent.map { it.id }.distinct().size)
        assertFalse(recent.any { it.id == track(0).id })
        assertTrue(recent.all { it.metadata.isEmpty() })
    }
    @Test fun dataSurvivesReaderRecreationAndCanBeCleared() = runTest {
        val store = MemoryPreferences()
        ListeningHistory(store).record(track(1))
        val secondReader = ListeningHistory(store)
        assertEquals(track(1).id, secondReader.recent.first().single().id)
        secondReader.clear()
        assertTrue(secondReader.recent.first().isEmpty())
    }
    @Test fun corruptOversizedAndFutureSnapshotsAreSafe() = runTest {
        val store = MemoryPreferences()
        val history = ListeningHistory(store)
        listOf("{bad json", "x".repeat(140_000), "{\"version\":99,\"entries\":[]}").forEach { invalid ->
            store.updateData { preferencesOf(stringPreferencesKey("recent") to invalid) }
            assertTrue(history.recent.first().isEmpty())
        }
        history.record(track(7))
        assertEquals(track(7).id, history.recent.first().single().id)
    }
    @Test fun pausedRestoreAndBufferingAreNotListeningAndPositionsDoNotRewrite() = runTest {
        val states = MutableStateFlow(PlaybackState(listOf(QueueEntry("first", track(1))), 0, status = PlaybackStatus.PAUSED))
        val recorded = mutableListOf<MusicId>()
        val job = backgroundScope.launchRecordings(states) { recorded += it.id }
        runCurrent(); assertTrue(recorded.isEmpty())
        states.value = states.value.copy(status = PlaybackStatus.BUFFERING, playWhenReady = true)
        runCurrent(); assertTrue(recorded.isEmpty())
        states.value = states.value.copy(status = PlaybackStatus.PLAYING)
        runCurrent(); assertEquals(listOf(track(1).id), recorded)
        states.value = states.value.copy(positionMs = 1000)
        runCurrent(); assertEquals(1, recorded.size)
        states.value = PlaybackState(listOf(QueueEntry("second", track(2))), 0, status = PlaybackStatus.PLAYING)
        runCurrent(); assertEquals(listOf(track(1).id, track(2).id), recorded)
        job.cancel()
    }
    @Test fun recordingFailureDoesNotStopLaterPlaybackObservation() = runTest {
        val states = MutableStateFlow(PlaybackState())
        val recorded = mutableListOf<MusicId>()
        var fail = true
        val job = backgroundScope.launchRecordings(states) { if (fail) { fail = false; throw IOException("disk full") }; recorded += it.id }
        runCurrent()
        states.value = PlaybackState(listOf(QueueEntry("a", track(1))), 0, status = PlaybackStatus.PLAYING)
        runCurrent()
        states.value = PlaybackState(listOf(QueueEntry("b", track(2))), 0, status = PlaybackStatus.PLAYING)
        runCurrent(); assertEquals(listOf(track(2).id), recorded)
        job.cancel()
    }
    @Test fun clearingHistoryAllowsANewListenToTheSameTrack() = runTest {
        val history = ListeningHistory(MemoryPreferences())
        val entry = QueueEntry("first-listen", track(1))
        val states = MutableStateFlow(PlaybackState(listOf(entry), 0, status = PlaybackStatus.PLAYING))
        val job = backgroundScope.launchRecordings(states, history::record)
        runCurrent(); assertEquals(track(1).id, history.recent.first().single().id)
        history.clear()
        states.value = states.value.copy(positionMs = 1000)
        runCurrent(); assertTrue(history.recent.first().isEmpty())
        states.value = states.value.copy(status = PlaybackStatus.PAUSED)
        runCurrent()
        states.value = states.value.copy(status = PlaybackStatus.PLAYING)
        runCurrent(); assertEquals(track(1).id, history.recent.first().single().id)
        history.clear()
        states.value = states.value.copy(queue = listOf(entry.copy(occurrenceId = "second-listen")))
        runCurrent(); assertEquals(track(1).id, history.recent.first().single().id)
        job.cancel()
    }
}

private class MemoryPreferences : DataStore<Preferences> {
    private val values = MutableStateFlow<Preferences>(emptyPreferences())
    private val mutex = Mutex()
    override val data = values.asStateFlow()
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = mutex.withLock {
        transform(values.value).also { values.value = it }
    }
}
