package dev.socialmusic.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.socialmusic.app.data.CatalogReader
import dev.socialmusic.common.LoadState
import dev.socialmusic.model.Track
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

@HiltViewModel
class CatalogViewModel @Inject constructor(private val catalog: CatalogReader) : ViewModel() {
    private val mutableState = MutableStateFlow<LoadState<List<Track>>>(LoadState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init { refresh() }
    fun refresh() {
        loadJob?.cancel()
        mutableState.value = LoadState.Loading
        loadJob = viewModelScope.launch {
            try {
                val tracks = catalog.load()
                currentCoroutineContext().ensureActive()
                mutableState.value = if (tracks.isEmpty()) LoadState.Empty else LoadState.Ready(tracks)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                mutableState.value = LoadState.Failed
            }
        }
    }
}
