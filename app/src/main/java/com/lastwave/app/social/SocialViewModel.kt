package com.lastwave.app.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lastwave.app.playback.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialUiState(
    val loading: Boolean = false, val homeLoaded: Boolean = false, val home: SocialHome = SocialHome(),
    val people: List<SocialPerson> = emptyList(), val recommendations: List<SocialRecommendation> = emptyList(),
    val message: String? = null,
)

@HiltViewModel
class SocialViewModel @Inject constructor(private val repository: SocialRepository, private val player: MusicPlayer) : ViewModel() {
    val configured = repository.configured
    val account = repository.account
    private val _state = MutableStateFlow(SocialUiState())
    val state = _state.asStateFlow()
    private var action: Job? = null
    private var generation = 0
    init {
        viewModelScope.launch { account.collectLatest { account ->
            generation++; action?.cancel(); action = null; _state.value = SocialUiState()
            if (account != null) refresh()
        } }
    }
    private fun perform(block: suspend () -> Unit) {
        if (action?.isActive == true) return
        val ownerGeneration = ++generation
        action = viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { if (generation == ownerGeneration) _state.update { it.copy(message = error.message ?: "Could not connect. Please try again.") } }
            finally { if (generation == ownerGeneration) _state.update { it.copy(loading = false) } }
        }
    }
    private fun invalidateRecommendations() {
        _state.update { it.copy(recommendations = emptyList(), people = emptyList(),
            home = it.home.copy(following = it.home.following.map { person -> person.copy(listening = null, influenceEnabled = false) })) }
    }
    private suspend fun loadHome(clearPeople: Boolean = false) {
        val home = repository.home()
        _state.update { it.copy(home = home, homeLoaded = true, people = if (clearPeople) emptyList() else it.people) }
    }
    fun refresh() = perform {
        val reloadRecommendations = _state.value.recommendations.isNotEmpty()
        invalidateRecommendations(); loadHome()
        if (reloadRecommendations) {
            val recommendations = repository.recommendations()
            _state.update { it.copy(recommendations = recommendations) }
        }
    }
    fun signIn(email: String, password: String) = perform { repository.signIn(email, password) }
    fun signUp(email: String, password: String) = perform {
        if (!repository.signUp(email, password)) _state.update { it.copy(message = "Check your email to confirm your account, then sign in.") }
    }
    fun signOut() { generation++; action?.cancel(); action = null; perform { repository.signOut() } }
    fun setupProfile(username: String, name: String) = perform {
        repository.setupProfile(username, name); loadHome()
    }
    fun search(query: String) = perform {
        val people = repository.search(query); _state.update { it.copy(people = people) }
    }
    fun follow(person: SocialPerson) = perform {
        invalidateRecommendations(); repository.follow(person.id, !person.following); loadHome(clearPeople = true)
    }
    fun influence(person: SocialPerson, enabled: Boolean) = perform {
        invalidateRecommendations(); repository.influence(person.id, enabled); loadHome(clearPeople = true)
    }
    fun hide(person: SocialPerson, hidden: Boolean) = perform {
        invalidateRecommendations(); repository.hide(person.id, hidden); loadHome(clearPeople = true)
    }
    fun block(person: SocialPerson) = perform {
        invalidateRecommendations(); repository.block(person.id, true); loadHome(clearPeople = true)
    }
    fun unblock(person: SocialPerson) = perform {
        invalidateRecommendations(); repository.block(person.id, false); loadHome(clearPeople = true)
    }
    fun preferences(value: SocialPreferences) = perform {
        invalidateRecommendations(); repository.setPreferences(value); loadHome(clearPeople = true)
    }
    fun recommendations() = perform {
        invalidateRecommendations()
        val recommendations = repository.recommendations()
        _state.update { it.copy(recommendations = recommendations) }
    }
    fun play(track: SharedListening) {
        runCatching { player.play(sharedTrack(track.videoId, track.title, track.artist), sourceLabel = "Onda friends") }
            .onFailure { error -> _state.update { it.copy(message = error.message) } }
    }
    fun play(track: SocialRecommendation) = play(SharedListening(track.videoId, track.title, track.artist, false))
    fun moreLike(track: SocialRecommendation) = perform {
        check(_state.value.home.preferences.personalizationEnabled && !_state.value.home.preferences.privateSession) { "Enable personalization and leave private session to give taste feedback." }
        repository.recordEvent(sharedTrack(track.videoId, track.title, track.artist), "like")
        _state.update { it.copy(message = "We'll use this to improve your recommendations.") }
    }
}
