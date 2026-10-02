package dev.socialmusic.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.socialmusic.app.R
import dev.socialmusic.common.LoadState
import dev.socialmusic.designsystem.Spacing
import dev.socialmusic.domain.music.PlaybackController

@Composable
fun HomeScreen(player: PlaybackController, onExplore: () -> Unit, viewModel: CatalogViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by player.state.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        item {
            Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Spacing.section))
            Text(stringResource(R.string.home_headline), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Spacing.medium))
            Text(stringResource(R.string.home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.large))
            FilledTonalButton(onClick = onExplore, modifier = Modifier.heightIn(min = Spacing.section + Spacing.medium)) {
                Icon(Icons.Outlined.Search, contentDescription = null)
                Spacer(Modifier.width(Spacing.small))
                Text(stringResource(R.string.find_music))
            }
            Spacer(Modifier.height(Spacing.section))
            Text(stringResource(R.string.discovery_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.discovery_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.medium))
        }
        when (val current = state) {
            LoadState.Loading -> item { MusicLoading(stringResource(R.string.loading_catalog)) }
            LoadState.Empty -> item { Text(stringResource(R.string.empty_catalog)) }
            LoadState.Failed -> item { MusicFailure(stringResource(R.string.catalog_error), viewModel::refresh) }
            is LoadState.Ready -> itemsIndexed(current.value, key = { index, track -> "$index:${track.id.canonical}" }) { index, track ->
                TrackRow(track, selected = playback.currentTrack?.id == track.id,
                    onPlay = { player.play(current.value, index) },
                    onPlayNext = { player.playNext(track) }, onAddToQueue = { player.addToQueue(track) })
            }
        }
    }
}
