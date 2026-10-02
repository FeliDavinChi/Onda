package dev.socialmusic.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.socialmusic.app.R
import dev.socialmusic.common.LoadState
import dev.socialmusic.designsystem.*
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.PlaybackState

@Composable
fun ExploreScreen(player: PlaybackController, padding: PaddingValues = PaddingValues(), viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playback by player.state.collectAsStateWithLifecycle()
    ExploreContent(state, playback, player, viewModel::updateQuery, viewModel::retry, padding)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExploreContent(
    state: SearchUiState, playback: PlaybackState, player: PlaybackController,
    onQuery: (String) -> Unit, onRetry: () -> Unit, padding: PaddingValues = PaddingValues(),
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val searchLabel = stringResource(R.string.search_music)
    LazyColumn(Modifier.fillMaxSize().testTag("search-results"), contentPadding = PaddingValues(
        start = Spacing.large, end = Spacing.large, top = padding.calculateTopPadding() + 24.dp,
        bottom = padding.calculateBottomPadding() + 24.dp), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        item {
            Text(stringResource(R.string.explore), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.explore_headline), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            GlassSurface(Modifier.fillMaxWidth(), GlassLevel.Standard) {
                TextField(value = state.query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).semantics { contentDescription = searchLabel }, singleLine = true,
                    placeholder = { Text(stringResource(R.string.search_music), style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = if (state.query.isNotEmpty()) ({
                        IconButton(onClick = { onQuery("") }) { Icon(Icons.Outlined.Close, stringResource(R.string.clear_search)) }
                    }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                )
            }
        }
        when (val results = state.results) {
            LoadState.Loading -> item { MusicLoading(stringResource(R.string.searching_music)) }
            LoadState.Failed -> item { MusicFailure(stringResource(R.string.search_error), onRetry) }
            LoadState.Empty -> item {
                Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(if (state.query.isBlank()) R.string.search_hint else R.string.no_matches),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    if (state.query.isBlank()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(R.string.genre_indie, R.string.genre_jazz, R.string.genre_electronic, R.string.genre_soul).forEach { resource ->
                                val genre = stringResource(resource)
                                SuggestionChip(onClick = { onQuery(genre) }, label = { Text(genre) }, modifier = Modifier.heightIn(min = 48.dp))
                            }
                        }
                    }
                }
            }
            is LoadState.Ready -> {
                item { Text(stringResource(R.string.songs), style = MaterialTheme.typography.titleLarge) }
                itemsIndexed(results.value, key = { index, track -> "$index:${track.id.canonical}" }) { index, track ->
                    TrackRow(track, playback.currentTrack?.id == track.id,
                        { player.play(results.value, index) }, { player.playNext(track) }, { player.addToQueue(track) })
                }
            }
        }
    }
}
