package com.lastwave.app.social

import androidx.lifecycle.viewModelScope
import com.lastwave.app.playback.MusicPlayer
import com.lastwave.app.playback.PlayableTrack
import com.lastwave.app.playback.PlaybackChromeState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SocialPrivacyTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<SocialRepository>(relaxed = true)
    private val player = mockk<MusicPlayer>(relaxed = true)
    private val preferences = MutableStateFlow(SocialPreferences(listeningShared = true, tasteShared = true))
    private val playback = MutableStateFlow(PlaybackChromeState())
    private val accounts = MutableStateFlow<OndaAccount?>(OndaAccount("owner", "test@example.invalid"))
    private val track = PlayableTrack(title = "Song", artist = "Artist", videoId = "abcdefghijk")
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { repository.configured } returns true
        every { repository.account } returns accounts
        every { repository.preferences } returns preferences
        every { player.chromeState } returns playback
        coEvery { repository.home() } returns SocialHome()
    }
    @After fun cleanup() { Dispatchers.resetMain(); unmockkAll() }

    @Test fun blockedActivityAndRecommendationsDisappearEvenIfRefreshFails() = runTest(dispatcher) {
        val friend = SocialPerson("friend", "friend", "Friend", following = true, listening = SharedListening("abcdefghijk", "Song", "Artist", true))
        coEvery { repository.home() } returns SocialHome(following = listOf(friend))
        coEvery { repository.recommendations() } returns listOf(SocialRecommendation("track", "abcdefghijk", "Song", "Artist", "Friend"))
        val model = SocialViewModel(repository, player)
        runCurrent(); model.recommendations(); runCurrent()
        assertEquals(1, model.state.value.recommendations.size)
        coEvery { repository.home() } throws java.io.IOException("Offline")
        model.block(friend); runCurrent()
        assertTrue(model.state.value.recommendations.isEmpty())
        assertNull(model.state.value.home.following.firstOrNull()?.listening)
        model.viewModelScope.cancel()
    }

    @Test fun listeningNeedsThirtyUninterruptedSecondsAndPrivateSessionCancelsIt() = runTest(dispatcher) {
        SocialListeningPublisher(repository, player, backgroundScope).start()
        playback.value = PlaybackChromeState(current = track, isPlaying = true)
        runCurrent(); advanceTimeBy(29_999); runCurrent()
        coVerify(exactly = 0) { repository.recordEvent(any(), any(), any()) }
        preferences.value = preferences.value.copy(privateSession = true)
        runCurrent(); advanceTimeBy(60_000); runCurrent()
        coVerify(exactly = 0) { repository.recordEvent(any(), any(), any()) }
        preferences.value = preferences.value.copy(privateSession = false)
        runCurrent(); advanceTimeBy(30_001); runCurrent()
        coVerify(exactly = 1) { repository.recordEvent(track, "listen", any()) }
    }

    @Test fun clearingPlaybackImmediatelyStopsPreviouslyPublishedListening() = runTest(dispatcher) {
        SocialListeningPublisher(repository, player, backgroundScope).start()
        playback.value = PlaybackChromeState(current = track, isPlaying = true)
        runCurrent()
        playback.value = PlaybackChromeState()
        runCurrent()
        coVerify { repository.publish(track, false) }
        coVerify(exactly = 0) { repository.recordEvent(any(), any(), any()) }
    }
}
