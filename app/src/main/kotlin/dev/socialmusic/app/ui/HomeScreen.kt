package dev.socialmusic.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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

@Composable
fun HomeScreen(player: PlaybackController, onExplore: () -> Unit, padding: PaddingValues = PaddingValues(), viewModel: CatalogViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by player.state.collectAsStateWithLifecycle()
    HomeContent(state, playback, player, onExplore, viewModel::refresh, padding)
}

@Composable
internal fun HomeContent(
    state: LoadState<List<Track>>, playback: PlaybackState, player: PlaybackController,
    onExplore: () -> Unit, onRetry: () -> Unit, padding: PaddingValues = PaddingValues(),
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(
        top = padding.calculateTopPadding() + 20.dp, bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        item {
            Column(Modifier.padding(horizontal = Spacing.large)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                    Icon(Icons.Outlined.GraphicEq, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(28.dp))
                Text(stringResource(R.string.home_headline), style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.large))
                GlassSurface(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onExplore), GlassLevel.Standard) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.search_music), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = Spacing.large, vertical = 4.dp)) {
                Text(stringResource(R.string.discovery_title), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.discovery_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when (val current = state) {
            LoadState.Loading -> item { Box(Modifier.padding(horizontal = Spacing.large)) { MusicLoading(stringResource(R.string.loading_catalog)) } }
            LoadState.Empty -> item { Text(stringResource(R.string.empty_catalog), Modifier.padding(horizontal = Spacing.large)) }
            LoadState.Failed -> item { Box(Modifier.padding(horizontal = Spacing.large)) { MusicFailure(stringResource(R.string.catalog_error), onRetry) } }
            is LoadState.Ready -> {
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = Spacing.large), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        itemsIndexed(current.value.take(6), key = { index, track -> "$index:${track.id.canonical}" }) { index, track ->
                            DiscoveryArtworkCard(track) { player.play(current.value, index) }
                        }
                    }
                }
                item {
                    Text(stringResource(R.string.more_to_discover), style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = Spacing.large, vertical = 4.dp))
                }
                itemsIndexed(current.value, key = { index, track -> "$index:${track.id.canonical}" }) { index, track ->
                    Box(Modifier.padding(horizontal = Spacing.medium)) {
                        TrackRow(track, playback.currentTrack?.id == track.id, { player.play(current.value, index) },
                            { player.playNext(track) }, { player.addToQueue(track) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryArtworkCard(track: Track, onPlay: () -> Unit) {
    val label = stringResource(R.string.play_track, track.title)
    Column(Modifier.width(208.dp).clickable(role = Role.Button, onClickLabel = label, onClick = onPlay)) {
        TrackArtwork(track, Modifier.fillMaxWidth().aspectRatio(1.12f), radius = 20.dp)
        Spacer(Modifier.height(12.dp))
        Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Text(track.artists.joinToString { it.name }.ifBlank { stringResource(R.string.unknown_artist) },
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
