package dev.socialmusic.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
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
            GlassSurface(Modifier.fillMaxWidth().padding(horizontal = Spacing.medium, vertical = Spacing.small), GlassLevel.Elevated, effects) {
                Column(Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)) {
                    if (error.recoverable) MusicFailure(stringResource(R.string.player_connection_error), player::retry)
                    else Text(stringResource(R.string.playback_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        return
    }
    GlassSurface(Modifier.fillMaxWidth().padding(horizontal = Spacing.medium, vertical = Spacing.small), GlassLevel.Elevated, effects) {
        Column {
            Row(Modifier.fillMaxWidth().padding(Spacing.small), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).clickable(onClickLabel = stringResource(R.string.open_player), onClick = onOpen).padding(Spacing.small), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    TrackArtwork(track, Modifier.size(48.dp))
                    Column(Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (state.error != null) stringResource(if (state.error?.code?.startsWith("QUEUE_") == true) R.string.queue_failed_short else R.string.playback_failed_short) else track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) }, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                PlayPauseButton(state, player::togglePlayPause, Modifier.size(48.dp))
            }
            if (state.buffering) LinearProgressIndicator(Modifier.fillMaxWidth().clearAndSetSemantics { })
            else if ((state.durationMs ?: 0) > 0) LinearProgressIndicator(
                progress = { (state.positionMs.toFloat() / state.durationMs!!.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().clearAndSetSemantics { },
            )
        }
    }
}

@Composable
private fun PlayPauseButton(state: PlaybackState, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val playing = state.playWhenReady && state.status != PlaybackStatus.ERROR
    FilledIconButton(onClick = onClick, modifier = modifier) {
        Icon(if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
            stringResource(if (playing) R.string.pause else R.string.play))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayer(state: PlaybackState, player: PlaybackController, onDismiss: () -> Unit) {
    val track = state.currentTrack
    var showQueue by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.small), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Outlined.KeyboardArrowDown, stringResource(R.string.close_player)) }
                    Text(stringResource(R.string.now_playing), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showQueue = true }, enabled = state.queue.isNotEmpty()) { Icon(Icons.AutoMirrored.Outlined.QueueMusic, stringResource(R.string.open_queue)) }
                }
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.large)) {
                    if (track == null) item {
                        val error = state.error
                        if (error?.recoverable == true) MusicFailure(stringResource(R.string.player_connection_error), player::retry)
                        else if (error != null) Text(stringResource(R.string.playback_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else Text(stringResource(R.string.queue_empty))
                    }
                    else {
                        item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TrackArtwork(track, Modifier.widthIn(max = 360.dp).fillMaxWidth().aspectRatio(1f), radius = 20.dp) } }
                        item {
                            Text(track.title, style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(Spacing.small))
                            Text(track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        item { SeekControl(state, player::seekTo) }
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = player::previous) { Icon(Icons.Outlined.SkipPrevious, stringResource(R.string.previous_track)) }
                                PlayPauseButton(state, player::togglePlayPause, Modifier.size(72.dp))
                                IconButton(onClick = player::next) { Icon(Icons.Outlined.SkipNext, stringResource(R.string.next_track)) }
                            }
                        }
                        item {
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                val shuffleState = stringResource(if (state.shuffle) R.string.shuffle_on else R.string.shuffle_off)
                                FilterChip(selected = state.shuffle, onClick = { player.setShuffle(!state.shuffle) },
                                    label = { Text(shuffleState) }, leadingIcon = { Icon(Icons.Outlined.Shuffle, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { stateDescription = shuffleState })
                                val repeatState = stringResource(when (state.repeat) { RepeatMode.OFF -> R.string.repeat_off; RepeatMode.ONE -> R.string.repeat_one; RepeatMode.ALL -> R.string.repeat_all })
                                FilterChip(selected = state.repeat != RepeatMode.OFF,
                                    onClick = { player.setRepeat(when (state.repeat) { RepeatMode.OFF -> RepeatMode.ALL; RepeatMode.ALL -> RepeatMode.ONE; RepeatMode.ONE -> RepeatMode.OFF }) },
                                    label = { Text(repeatState) },
                                    leadingIcon = { Icon(if (state.repeat == RepeatMode.ONE) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { stateDescription = repeatState })
                            }
                        }
                        if (state.buffering) item { MusicLoading(stringResource(R.string.preparing_audio)) }
                        state.error?.let { error -> item {
                            if (error.recoverable) MusicFailure(stringResource(if (error.code.startsWith("QUEUE_")) R.string.queue_error else R.string.playback_error), player::retry)
                            else Text(stringResource(R.string.playback_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } }
                        item {
                            OutlinedButton(onClick = { showQueue = true }, modifier = Modifier.fillMaxWidth()) { Text(pluralStringResource(R.plurals.queue_count, state.queue.size, state.queue.size)) }
                        }
                    }
                }
            }
        }
        if (showQueue) QueueSheet(state, player) { showQueue = false }
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
            Text(elapsed, style = MaterialTheme.typography.bodySmall)
            Text(duration?.let(::formatTime) ?: stringResource(R.string.duration_unknown), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(state: PlaybackState, player: PlaybackController, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.fillMaxWidth().navigationBarsPadding(), contentPadding = PaddingValues(horizontal = Spacing.large, vertical = Spacing.medium), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.queue), style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = player::clear, enabled = state.queue.isNotEmpty()) { Text(stringResource(R.string.clear_queue)) }
                }
            }
            if (state.queue.isEmpty()) item { Text(stringResource(R.string.queue_empty)) }
            itemsIndexed(state.queue, key = { _, entry -> entry.occurrenceId }) { index, entry ->
                Column(Modifier.fillMaxWidth().semantics { selected = index == state.currentIndex }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        TrackArtwork(entry.track, Modifier.size(48.dp))
                        Column(Modifier.weight(1f)) {
                            Text(entry.track.title, style = MaterialTheme.typography.titleMedium)
                            Text(entry.track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (index == state.currentIndex) Text(stringResource(R.string.now_playing), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { player.move(entry.occurrenceId, index - 1) }, enabled = index > 0) { Icon(Icons.Outlined.ArrowUpward, stringResource(R.string.move_track_up, entry.track.title)) }
                        IconButton(onClick = { player.move(entry.occurrenceId, index + 1) }, enabled = index < state.queue.lastIndex) { Icon(Icons.Outlined.ArrowDownward, stringResource(R.string.move_track_down, entry.track.title)) }
                        IconButton(onClick = { player.remove(entry.occurrenceId) }) { Icon(Icons.Outlined.Close, stringResource(R.string.remove_track, entry.track.title)) }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
