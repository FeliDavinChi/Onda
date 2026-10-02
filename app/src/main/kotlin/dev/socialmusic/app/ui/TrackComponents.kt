package dev.socialmusic.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import dev.socialmusic.app.R
import dev.socialmusic.designsystem.Spacing
import dev.socialmusic.model.Track

@Composable
fun TrackArtwork(track: Track, modifier: Modifier = Modifier, radius: Dp = 12.dp) {
    Box(modifier.clip(RoundedCornerShape(radius)).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primaryContainer))), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
        if (track.artwork != null) AsyncImage(model = track.artwork, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun TrackRow(track: Track, selected: Boolean, onPlay: () -> Unit, onPlayNext: () -> Unit, onAddToQueue: () -> Unit) {
    var menuOpen by remember(track.id) { mutableStateOf(false) }
    val playLabel = stringResource(R.string.play_track, track.title)
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
        .background(if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = .45f) else Color.Transparent)
        .clickable(onClickLabel = playLabel, onClick = onPlay).semantics { this.selected = selected }
        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TrackArtwork(track, Modifier.size(54.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (selected) Text(stringResource(R.string.in_player), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) { Icon(Icons.Outlined.MoreVert, stringResource(R.string.track_actions, track.title)) }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.play_next)) }, onClick = { menuOpen = false; onPlayNext() })
                DropdownMenuItem(text = { Text(stringResource(R.string.add_to_queue)) }, onClick = { menuOpen = false; onAddToQueue() })
            }
        }
    }
}

@Composable
fun MusicLoading(label: String) {
    Row(Modifier.padding(vertical = Spacing.medium), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        CircularProgressIndicator(Modifier.size(24.dp))
        Text(label)
    }
}

@Composable
fun MusicFailure(message: String, onRetry: () -> Unit) {
    Column(Modifier.padding(vertical = Spacing.medium)) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

internal fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
