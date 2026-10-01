package dev.socialmusic.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.socialmusic.app.data.VisualPreferences
import dev.socialmusic.common.VisualEffectLevel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PreferencesViewModel @Inject constructor(private val preferences: VisualPreferences) : ViewModel() {
    val effects = preferences.effects.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VisualEffectLevel.FULL)
    private val mutableSaveError = MutableStateFlow(false)
    val saveError = mutableSaveError.asStateFlow()
    fun select(level: VisualEffectLevel) {
        viewModelScope.launch {
            mutableSaveError.value = false
            try { preferences.setEffects(level) }
            catch (_: IOException) { mutableSaveError.value = true }
        }
    }
}
