package dev.socialmusic.app.ui

import dev.socialmusic.app.data.TrackSearcher
import dev.socialmusic.common.LoadState
import dev.socialmusic.model.MusicId
import dev.socialmusic.model.Track
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val song = Track(MusicId("youtube", "abcdefghijk"), "Real song", emptyList())
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun blankQueryClearsResultsWithoutSearching() = runTest(dispatcher) {
        var calls = 0
        val model = SearchViewModel(TrackSearcher { calls++; listOf(song) })
        model.updateQuery("   ")
        advanceUntilIdle()
        assertEquals(0, calls)
        assertEquals(LoadState.Empty, model.state.value.results)
    }

    @Test fun searchWaits350MillisecondsAndUsesTrimmedQuery() = runTest(dispatcher) {
        val queries = mutableListOf<String>()
        val model = SearchViewModel(TrackSearcher { queries += it; listOf(song) })
        model.updateQuery("  jazz  ")
        advanceTimeBy(349)
        runCurrent()
        assertTrue(queries.isEmpty())
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("jazz"), queries)
        assertEquals(LoadState.Ready(listOf(song)), model.state.value.results)
    }

    @Test fun newerQueryCancelsThePreviousRequest() = runTest(dispatcher) {
        var cancelled = false
        val model = SearchViewModel(TrackSearcher { query ->
            if (query == "old") {
                try { delay(2000) } finally { cancelled = true }
            }
            listOf(song)
        })
        model.updateQuery("old")
        advanceTimeBy(350)
        runCurrent()
        model.updateQuery("new")
        advanceUntilIdle()
        assertTrue(cancelled)
        assertEquals("new", model.state.value.query)
        assertEquals(LoadState.Ready(listOf(song)), model.state.value.results)
    }

    @Test fun lateNonCooperativeResultCannotReplaceNewResults() = runTest(dispatcher) {
        val model = SearchViewModel(TrackSearcher { query ->
            if (query == "old") withContext(NonCancellable) { delay(2000); emptyList() }
            else listOf(song)
        })
        model.updateQuery("old")
        advanceTimeBy(350)
        runCurrent()
        model.updateQuery("new")
        advanceUntilIdle()
        assertEquals(LoadState.Ready(listOf(song)), model.state.value.results)
    }

    @Test fun failedSearchCanBeRetriedWithoutEditingQuery() = runTest(dispatcher) {
        var calls = 0
        val model = SearchViewModel(TrackSearcher {
            if (++calls == 1) throw IOException("offline")
            listOf(song)
        })
        model.updateQuery("jazz")
        advanceUntilIdle()
        assertEquals(LoadState.Failed, model.state.value.results)
        model.retry()
        runCurrent()
        assertEquals(LoadState.Ready(listOf(song)), model.state.value.results)
        assertEquals(2, calls)
    }

    @Test fun noMatchesProduceAnExplicitEmptyState() = runTest(dispatcher) {
        val model = SearchViewModel(TrackSearcher { emptyList() })
        model.updateQuery("nothing")
        advanceUntilIdle()
        assertEquals(LoadState.Empty, model.state.value.results)
    }
}
