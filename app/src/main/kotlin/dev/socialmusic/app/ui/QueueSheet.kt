package dev.socialmusic.app.ui

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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import dev.socialmusic.app.R
import dev.socialmusic.designsystem.*
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QueueSheet(state: PlaybackState, player: PlaybackController, onDismiss: () -> Unit) {
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
