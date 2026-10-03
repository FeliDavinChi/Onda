package dev.socialmusic.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.socialmusic.app.R
import dev.socialmusic.designsystem.*
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.*

@Composable
fun MiniPlayer(state: PlaybackState, player: PlaybackController, effects: EffectLevel, onOpen: () -> Unit) {
    val track = state.currentTrack
    if (track == null) {
        state.error?.let { error ->
            GlassSurface(Modifier.fillMaxWidth(), GlassLevel.Elevated, effects) {
                Column(Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)) {
                    if (error.recoverable) MusicFailure(stringResource(R.string.player_connection_error), player::retry)
                    else Text(stringResource(R.string.playback_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }
    GlassSurface(Modifier.fillMaxWidth().testTag("mini-player"), GlassLevel.Elevated, effects, radius = 24.dp) {
        Column {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_player), onClick = onOpen)
                    .heightIn(min = 56.dp).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrackArtwork(track, Modifier.size(48.dp), radius = 16.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (state.error != null) stringResource(if (state.error?.code?.startsWith("QUEUE_") == true) R.string.queue_failed_short else R.string.playback_failed_short)
                            else track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                PlayPauseButton(state, player::togglePlayPause, Modifier.size(48.dp))
                IconButton(onClick = tactileAction(player::next)) { Icon(Icons.Outlined.SkipNext, stringResource(R.string.next_track)) }
            }
            if (state.buffering) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp).clearAndSetSemantics { })
            else if ((state.durationMs ?: 0) > 0) LinearProgressIndicator(
                progress = { (state.positionMs.toFloat() / state.durationMs!!.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(2.dp).clearAndSetSemantics { },
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .08f),
            )
        }
    }
}

@Composable
internal fun PlayPauseButton(state: PlaybackState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val playing = state.playWhenReady && state.status != PlaybackStatus.ERROR
    FilledIconButton(onClick = tactileAction(onClick), modifier = modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)) {
        Icon(if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
            stringResource(if (playing) R.string.pause else R.string.play), modifier = Modifier.size(28.dp))
    }
}

@Composable
fun FullPlayer(state: PlaybackState, player: PlaybackController, effects: EffectLevel = LocalEffectLevel.current, onDismiss: () -> Unit) {
    val track = state.currentTrack
    var showQueue by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        ArtworkTheme(track, effects) { GlassBackdropScope(effects) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.matchParentSize().glassBackdropSource()) {
                    WaveBackdrop(Modifier.matchParentSize())
                }
                Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) { Icon(Icons.Outlined.KeyboardArrowDown, stringResource(R.string.close_player)) }
                        Text(stringResource(R.string.now_playing), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showQueue = true }, enabled = state.queue.isNotEmpty()) {
                            Icon(Icons.AutoMirrored.Outlined.QueueMusic, stringResource(R.string.open_queue))
                        }
                    }
                    LazyColumn(Modifier.widthIn(max = 460.dp).fillMaxWidth().weight(1f), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        if (track == null) item {
                            val error = state.error
                            if (error?.recoverable == true) MusicFailure(stringResource(R.string.player_connection_error), player::retry)
                            else if (error != null) Text(stringResource(R.string.playback_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else Text(stringResource(R.string.queue_empty))
                        }
                        else {
                            item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                TrackArtwork(track, Modifier.widthIn(max = 380.dp).fillMaxWidth().aspectRatio(1f), radius = 20.dp)
                            } }
                            item {
                                Text(track.title, style = MaterialTheme.typography.headlineLarge)
                                Spacer(Modifier.height(6.dp))
                                Text(track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) },
                                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            item {
                                SeekControl(state, player::seekTo)
                            }
                            item {
                                GlassSurface(Modifier.widthIn(max = 420.dp).fillMaxWidth(), GlassLevel.Elevated, radius = 40.dp) {
                                    Box(Modifier.padding(horizontal = 8.dp, vertical = 12.dp)) { PlayerTransport(state, player) }
                                }
                            }
                            if (state.buffering) item { MusicLoading(stringResource(R.string.preparing_audio)) }
                            state.error?.let { error -> item {
                                if (error.recoverable) MusicFailure(stringResource(if (error.code.startsWith("QUEUE_")) R.string.queue_error else R.string.playback_error), player::retry)
                                else Text(stringResource(R.string.playback_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } }
                            item {
                                PlayerSecondaryControls(state, player) { showQueue = true }
                            }
                        }
                    }
                }
            }
        } }
        if (showQueue) QueueSheet(state, player) { showQueue = false }
    }
}

@Composable
private fun PlayerTransport(state: PlaybackState, player: PlaybackController) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = tactileAction(player::previous), modifier = Modifier.size(56.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Icon(Icons.Outlined.SkipPrevious, stringResource(R.string.previous_track), Modifier.size(28.dp))
        }
        PlayPauseButton(state, player::togglePlayPause, Modifier.size(72.dp))
        FilledTonalIconButton(onClick = tactileAction(player::next), modifier = Modifier.size(56.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
            Icon(Icons.Outlined.SkipNext, stringResource(R.string.next_track), Modifier.size(28.dp))
        }
    }
}

/** LastWave's primary transport / lower modes hierarchy, with a real Onda queue in the center. */
@Composable
private fun PlayerSecondaryControls(state: PlaybackState, player: PlaybackController, onQueue: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        val shuffleLabel = stringResource(if (state.shuffle) R.string.shuffle_on else R.string.shuffle_off)
        IconToggleButton(checked = state.shuffle, onCheckedChange = { player.setShuffle(it) },
            modifier = Modifier.semantics { stateDescription = shuffleLabel }) {
            Icon(Icons.Outlined.Shuffle, shuffleLabel, tint = if (state.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onQueue, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
            Text(pluralStringResource(R.plurals.queue_count, state.queue.size, state.queue.size))
        }
        val repeatLabel = stringResource(when (state.repeat) { RepeatMode.OFF -> R.string.repeat_off; RepeatMode.ONE -> R.string.repeat_one; RepeatMode.ALL -> R.string.repeat_all })
        IconButton(onClick = { player.setRepeat(when (state.repeat) { RepeatMode.OFF -> RepeatMode.ALL; RepeatMode.ALL -> RepeatMode.ONE; RepeatMode.ONE -> RepeatMode.OFF }) },
            modifier = Modifier.semantics { stateDescription = repeatLabel; selected = state.repeat != RepeatMode.OFF }) {
            Icon(if (state.repeat == RepeatMode.ONE) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat, repeatLabel,
                tint = if (state.repeat != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SeekControl(state: PlaybackState, seekTo: (Long) -> Unit) {
    val duration = state.durationMs?.takeIf { it > 0 }
    val occurrence = state.queue.getOrNull(state.currentIndex)?.occurrenceId
    var dragPosition by remember(occurrence, duration) { mutableStateOf<Float?>(null) }
    val position = dragPosition ?: state.positionMs.toFloat()
    val seekLabel = stringResource(R.string.seek)
    val elapsed = formatTime(position.toLong())
    val total = duration?.let(::formatTime) ?: stringResource(R.string.duration_unavailable)
    val seekState = stringResource(R.string.seek_time_description, elapsed, total)
    Column {
        Slider(value = position.coerceIn(0f, (duration ?: 1).toFloat()), onValueChange = { dragPosition = it },
            onValueChangeFinished = { dragPosition?.let { seekTo(it.toLong()) }; dragPosition = null },
            enabled = duration != null, valueRange = 0f..(duration ?: 1).toFloat(),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = seekLabel; stateDescription = seekState })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(elapsed, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val remaining = duration?.let { formatTime((it - position.toLong()).coerceAtLeast(0)) }
            val remainingDescription = remaining?.let { stringResource(R.string.time_remaining, it) } ?: stringResource(R.string.duration_unavailable)
            Text(remaining?.let { "−$it" } ?: stringResource(R.string.duration_unknown), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clearAndSetSemantics { contentDescription = remainingDescription })
        }
    }
}
