package dev.socialmusic.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.socialmusic.app.R
import dev.socialmusic.common.LoadState
import dev.socialmusic.designsystem.*
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.PlaybackState
import dev.socialmusic.model.Track

private enum class HomeCollection { Recent, Recommended }

@Composable
fun HomeScreen(player: PlaybackController, onExplore: () -> Unit, padding: PaddingValues = PaddingValues(), viewModel: CatalogViewModel = hiltViewModel(), recentlyPlayed: List<Track> = emptyList(), onOpenPlayer: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by player.state.collectAsStateWithLifecycle()
    HomeContent(state, playback, player, onExplore, viewModel::refresh, padding, recentlyPlayed, onOpenPlayer)
}

/** Statistics -> selected collection -> pinned playback follows LastWave's Home structure.
 * Adapted for Onda's private local history and music contracts; see THIRD_PARTY_NOTICES.md. */
@Composable
internal fun HomeContent(
    state: LoadState<List<Track>>, playback: PlaybackState, player: PlaybackController,
    onExplore: () -> Unit, onRetry: () -> Unit, padding: PaddingValues = PaddingValues(), recentlyPlayed: List<Track> = emptyList(), onOpenPlayer: () -> Unit = {},
) {
    val tracks = (state as? LoadState.Ready)?.value.orEmpty()
    var choice by rememberSaveable { mutableStateOf<HomeCollection?>(null) }
    val collection = choice?.takeUnless { it == HomeCollection.Recent && recentlyPlayed.isEmpty() }
        ?: if (recentlyPlayed.isNotEmpty()) HomeCollection.Recent else HomeCollection.Recommended
    var showAll by rememberSaveable(collection) { mutableStateOf(false) }
    val source = if (collection == HomeCollection.Recent) recentlyPlayed else tracks
    val rows = source.mapIndexed { index, track -> index to track }.filter { it.second.id != playback.currentTrack?.id }
    val discovery = tracks.mapIndexed { index, track -> index to track }.filter { it.second.id != playback.currentTrack?.id }.take(3)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize().testTag("home-content"), contentPadding = PaddingValues(
            top = padding.calculateTopPadding() + 20.dp, bottom = padding.calculateBottomPadding() + 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.large), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OndaMark(Modifier.size(32.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                    GlassSurface(Modifier.size(48.dp), GlassLevel.Standard, radius = 24.dp) {
                        IconButton(onClick = tactileAction(onExplore)) { Icon(Icons.Outlined.Search, stringResource(R.string.search_music)) }
                    }
                }
            }
            if (recentlyPlayed.isNotEmpty()) item { ListeningStats(recentlyPlayed) }
            item { CollectionHeader(collection, recentlyPlayed.isNotEmpty()) { choice = it } }
            playback.currentTrack?.let { track -> item {
                Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.medium).testTag("current-listening")
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.open_player), onClick = onOpenPlayer)
                    .padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrackArtwork(track, Modifier.size(64.dp), radius = 14.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        ArtistLine(track, small = true)
                        Text(stringResource(R.string.in_player), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    PlayPauseButton(playback, player::togglePlayPause, Modifier.size(48.dp))
                }
            } }
            if (collection == HomeCollection.Recommended) when (state) {
                LoadState.Loading -> item { Box(Modifier.padding(horizontal = Spacing.large)) { MusicLoading(stringResource(R.string.loading_catalog)) } }
                LoadState.Failed -> item { Box(Modifier.padding(horizontal = Spacing.large)) { MusicFailure(stringResource(R.string.catalog_error), onRetry) } }
                LoadState.Empty -> item { Text(stringResource(R.string.empty_catalog), Modifier.padding(horizontal = Spacing.large)) }
                is LoadState.Ready -> Unit
            }
            itemsIndexed(if (showAll) rows else rows.take(4), key = { _, row -> "${collection.name}:${row.second.id.canonical}" }) { _, (index, track) ->
                Box(Modifier.padding(horizontal = Spacing.medium).testTag("mix-row-$index")) {
                    TrackRow(track, false, { player.play(source, index) }, { player.playNext(track) }, { player.addToQueue(track) })
                }
            }
            if (rows.size > 4) item {
                TextButton(onClick = { showAll = !showAll }, modifier = Modifier.padding(horizontal = Spacing.medium).heightIn(min = 48.dp)) {
                    Text(stringResource(if (showAll) R.string.show_less else R.string.show_all))
                }
            }
            if (discovery.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.discovery_title), stringResource(R.string.discovery_description)) }
                item { DiscoveryBento(discovery.map { it.second }) { offset -> player.play(tracks, discovery[offset].first) } }
            }
            item {
                Column(Modifier.padding(horizontal = Spacing.large), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.friend_activity), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.friend_activity_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CollectionHeader(collection: HomeCollection, hasHistory: Boolean, onSelect: (HomeCollection) -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val label = stringResource(if (collection == HomeCollection.Recent) R.string.recently_played else R.string.recommended)
    val selector: @Composable () -> Unit = {
        Box {
            TextButton(onClick = { menu = true }, modifier = Modifier.heightIn(min = 48.dp).testTag("home-collection")) {
                Text(label, maxLines = 2)
                Spacer(Modifier.width(6.dp)); Icon(Icons.Outlined.ExpandMore, null, Modifier.size(20.dp))
            }
            DropdownMenu(menu, { menu = false }, containerColor = androidx.compose.ui.graphics.Color.Transparent, shadowElevation = 0.dp) {
                WindowGlassSurface {
                    Column {
                        HomeCollection.entries.forEach { item ->
                            DropdownMenuItem(text = { Text(stringResource(if (item == HomeCollection.Recent) R.string.recently_played else R.string.recommended)) },
                                onClick = { menu = false; onSelect(item) }, enabled = item != HomeCollection.Recent || hasHistory,
                                modifier = Modifier.semantics { selected = item == collection })
                        }
                    }
                }
            }
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = Spacing.large)) {
        if (LocalDensity.current.fontScale >= 1.5f || maxWidth < 320.dp) Column {
            Text(stringResource(R.string.your_mix), style = MaterialTheme.typography.titleLarge); selector()
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.your_mix), style = MaterialTheme.typography.titleLarge); selector()
        }
    }
}

@Composable
private fun ListeningStats(tracks: List<Track>) {
    val artistCount = tracks.flatMap { it.artists }.filter { it.name.isNotBlank() }.distinctBy { it.id }.size
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.large), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.recent_listening), style = MaterialTheme.typography.titleLarge)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val songs: @Composable (Modifier) -> Unit = { StatTile(tracks.size.toString(), stringResource(R.string.songs), it, true) }
            val artists: @Composable (Modifier) -> Unit = { StatTile(if (artistCount == 0) "—" else artistCount.toString(), stringResource(R.string.artists), it, false) }
            if (LocalDensity.current.fontScale >= 1.5f || maxWidth < 320.dp) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                songs(Modifier.fillMaxWidth()); artists(Modifier.fillMaxWidth())
            } else Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                songs(Modifier.weight(1.4f)); artists(Modifier.weight(1f))
            }
        }
        Text(stringResource(R.string.recent_stats_detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier, emphasis: Boolean) {
    Column(modifier.background(if (emphasis) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        RoundedCornerShape(if (emphasis) 24.dp else 18.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = if (emphasis) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineLarge)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(title: String, description: String? = null) {
    Column(Modifier.padding(horizontal = Spacing.large), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DiscoveryBento(tracks: List<Track>, onPlay: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = Spacing.large)) {
        if (maxWidth < 320.dp || LocalDensity.current.fontScale >= 1.5f) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                tracks.forEachIndexed { index, track -> ArtworkTile(track, Modifier.fillMaxWidth().testTag("discovery-track-$index"), 18.dp, 1.35f) { onPlay(index) } }
            }
        } else Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ArtworkTile(tracks.first(), Modifier.weight(1.15f).testTag("discovery-track-0"), 18.dp, .72f) { onPlay(0) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                tracks.drop(1).forEachIndexed { index, track -> ArtworkTile(track, Modifier.fillMaxWidth().testTag("discovery-track-${index + 1}"), 12.dp, 1.8f) { onPlay(index + 1) } }
            }
        }
    }
}

@Composable
private fun ArtworkTile(track: Track, modifier: Modifier, radius: androidx.compose.ui.unit.Dp, ratio: Float = 1f, onPlay: () -> Unit) {
    val play = tactileAction(onPlay)
    Column(modifier.clickable(role = Role.Button, onClickLabel = stringResource(R.string.play_track, track.title), onClick = play)) {
        TrackArtwork(track, Modifier.fillMaxWidth().aspectRatio(ratio), radius = radius)
        Spacer(Modifier.height(10.dp))
        Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        ArtistLine(track, small = true)
    }
}

@Composable
private fun ArtistLine(track: Track, small: Boolean = false) {
    Text(track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) },
        style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
}
