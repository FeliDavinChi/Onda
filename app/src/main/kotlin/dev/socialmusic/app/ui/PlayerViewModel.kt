package dev.socialmusic.app.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.socialmusic.domain.music.PlaybackController
import javax.inject.Inject

/** Activity-scoped presentation holder. The singleton controller connects to the service-owned player. */
@HiltViewModel
class PlayerViewModel @Inject constructor(val controller: PlaybackController) : ViewModel()
