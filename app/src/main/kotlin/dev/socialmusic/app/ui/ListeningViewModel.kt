package dev.socialmusic.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.socialmusic.app.data.ListeningHistory
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ListeningViewModel @Inject constructor(private val history: ListeningHistory) : ViewModel() {
    val recent = history.recent.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val mutableError = MutableStateFlow(false)
    val clearError = mutableError.asStateFlow()
    fun clear() { viewModelScope.launch {
        mutableError.value = false
        try { history.clear() } catch (_: IOException) { mutableError.value = true }
    } }
}
