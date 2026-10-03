package com.lastwave.app.social

import com.lastwave.app.playback.MusicPlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Observes the existing player; failures here can never interrupt playback. */
@Singleton
class SocialListeningPublisher @Inject constructor(
    private val repository: SocialRepository,
    private val player: MusicPlayer,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null
    fun start() {
        if (job != null || !repository.configured) return
        job = scope.launch {
            repository.account.collectLatest { account ->
                if (account == null) return@collectLatest
                safely { repository.home() }
                var lastVideo: String? = null
                var recorded = false
                var lastPublished: com.lastwave.app.playback.PlayableTrack? = null
                var eventId = UUID.randomUUID().toString()
                combine(player.chromeState, repository.preferences) { playback, preferences -> playback to preferences }
                    .collectLatest { (playback, preferences) ->
                        val track = playback.current
                        val video = track?.videoId
                        if (track == null || video == null || !YouTubeIdentity.matches(video)) {
                            if (preferences.listeningShared && !preferences.privateSession) {
                                lastPublished?.let { safely { repository.publish(it, false) } }
                            }
                            lastPublished = null
                            return@collectLatest
                        }
                        if (video != lastVideo) { lastVideo = video; recorded = false; eventId = UUID.randomUUID().toString() }
                        if (preferences.privateSession) return@collectLatest
                        val playing = playback.isPlaying && !playback.isBuffering
                        if (preferences.listeningShared && safely { repository.publish(track, playing) }) lastPublished = track
                        if (!playing) return@collectLatest
                        while (true) {
                            // At least 30 uninterrupted seconds before a taste signal.
                            delay(30_000)
                            if (!recorded && (preferences.personalizationEnabled || preferences.tasteShared)) {
                                recorded = safely { repository.recordEvent(track, "listen", eventId) }
                            }
                            if (preferences.listeningShared) safely { repository.publish(track, true) }
                        }
                    }
            }
        }
    }
    private suspend fun safely(block: suspend () -> Unit): Boolean = try { block(); true }
    catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { false }
}
