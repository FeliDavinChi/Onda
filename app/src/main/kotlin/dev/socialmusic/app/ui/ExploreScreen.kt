package dev.socialmusic.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.socialmusic.app.R
import dev.socialmusic.common.LoadState
import dev.socialmusic.designsystem.Spacing
import dev.socialmusic.domain.music.PlaybackController

@Composable
fun ExploreScreen(player: PlaybackController, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by player.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.small)) {
        item {
            Column(Modifier.padding(horizontal = Spacing.small, vertical = Spacing.medium)) {
                Text(stringResource(R.string.explore_headline), style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(Spacing.medium))
                OutlinedTextField(
                    value = state.query, onValueChange = viewModel::updateQuery, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text(stringResource(R.string.search_music)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = if (state.query.isNotEmpty()) ({
                        IconButton(onClick = { viewModel.updateQuery("") }) { Icon(Icons.Outlined.Close, stringResource(R.string.clear_search)) }
                    }) else null,
                )
            }
        }
        when (val results = state.results) {
            LoadState.Loading -> item { MusicLoading(stringResource(R.string.searching_music)) }
            LoadState.Failed -> item { MusicFailure(stringResource(R.string.search_error), viewModel::retry) }
            LoadState.Empty -> item {
                Text(stringResource(if (state.query.isBlank()) R.string.search_hint else R.string.no_matches), modifier = Modifier.padding(Spacing.small), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is LoadState.Ready -> itemsIndexed(results.value, key = { index, track -> "$index:${track.id.canonical}" }) { index, track ->
                TrackRow(track, selected = playback.currentTrack?.id == track.id,
                    onPlay = { player.play(results.value, index) },
                    onPlayNext = { player.playNext(track) }, onAddToQueue = { player.addToQueue(track) })
            }
        }
    }
}
