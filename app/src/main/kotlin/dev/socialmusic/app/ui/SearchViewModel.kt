package dev.socialmusic.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import dev.socialmusic.app.data.TrackSearcher
import dev.socialmusic.common.LoadState
import dev.socialmusic.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchUiState(val query: String = "", val results: LoadState<List<Track>> = LoadState.Empty)

@HiltViewModel
class SearchViewModel @Inject constructor(private val searcher: TrackSearcher) : ViewModel() {
    private val mutableState = MutableStateFlow(SearchUiState())
    val state = mutableState.asStateFlow()
    private var request: Job? = null
    private var generation = 0L
    fun updateQuery(query: String) {
        mutableState.value = SearchUiState(query = query.take(500))
        search(debounce = true)
    }
    fun retry() = search(debounce = false)
    private fun search(debounce: Boolean) {
        request?.cancel()
        val expected = ++generation
        val query = mutableState.value.query.trim()
        if (query.isEmpty()) { mutableState.value = mutableState.value.copy(results = LoadState.Empty); return }
        mutableState.value = mutableState.value.copy(results = LoadState.Loading)
        request = viewModelScope.launch {
            if (debounce) delay(350)
            val results = try { searcher.search(query).let { if (it.isEmpty()) LoadState.Empty else LoadState.Ready(it) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { LoadState.Failed }
            if (expected == generation) mutableState.value = mutableState.value.copy(results = results)
        }
    }
}
