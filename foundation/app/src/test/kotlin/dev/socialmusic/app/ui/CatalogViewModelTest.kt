package dev.socialmusic.app.ui

import dev.socialmusic.app.data.CatalogReader
import dev.socialmusic.common.LoadState
import dev.socialmusic.model.*
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val track = Track(MusicId("demo", "track"), "Sample", emptyList())
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun loadingBecomesRealCatalogData() = runTest(dispatcher) {
        val model = CatalogViewModel(CatalogReader { listOf(track) })
        assertEquals(LoadState.Loading, model.state.value)
        advanceUntilIdle()
        assertEquals(LoadState.Ready(listOf(track)), model.state.value)
    }
    @Test fun emptyCatalogHasExplicitEmptyState() = runTest(dispatcher) {
        val model = CatalogViewModel(CatalogReader { emptyList() })
        advanceUntilIdle()
        assertEquals(LoadState.Empty, model.state.value)
    }
    @Test fun failedRequestCanBeRetried() = runTest(dispatcher) {
        var attempts = 0
        val model = CatalogViewModel(CatalogReader {
            attempts++
            if (attempts == 1) throw IOException("No connection")
            listOf(track)
        })
        advanceUntilIdle()
        assertEquals(LoadState.Failed, model.state.value)
        model.refresh()
        advanceUntilIdle()
        assertEquals(LoadState.Ready(listOf(track)), model.state.value)
    }
    @Test fun refreshedRequestCancelsThePreviousLoad() = runTest(dispatcher) {
        var attempts = 0
        var oldCancelled = false
        val model = CatalogViewModel(CatalogReader {
            attempts++
            if (attempts == 1) {
                try { delay(1000) } finally { oldCancelled = true }
                emptyList()
            } else listOf(track)
        })
        runCurrent()
        model.refresh()
        advanceUntilIdle()
        assertTrue(oldCancelled)
        assertEquals(LoadState.Ready(listOf(track)), model.state.value)
    }
}
