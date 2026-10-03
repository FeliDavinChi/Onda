package dev.socialmusic.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.socialmusic.app.R
import dev.socialmusic.common.LoadState
import dev.socialmusic.designsystem.Spacing
import dev.socialmusic.model.Track

@Composable
fun HomeScreen(viewModel: CatalogViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        item {
            Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.section))
            Text(stringResource(R.string.home_headline), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(Spacing.small))
            Text(stringResource(R.string.home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Spacer(Modifier.height(Spacing.large))
            Text(stringResource(R.string.demo_catalog), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.demo_catalog_description), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when (val current = state) {
            LoadState.Loading -> item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                    Text(stringResource(R.string.loading_catalog))
                }
            }
            LoadState.Empty -> item { Text(stringResource(R.string.empty_catalog)) }
            LoadState.Failed -> item {
                Text(stringResource(R.string.catalog_error))
                TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.retry)) }
            }
            is LoadState.Ready -> items(current.value, key = { it.id.canonical }) { TrackRow(it) }
        }
    }
}

@Composable
private fun TrackRow(track: Track) {
    ListItem(
        headlineContent = { Text(track.title) },
        supportingContent = { Text(track.artists.joinToString { it.name }) },
        leadingContent = {
            Box(
                Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (track.artwork == null) Icon(Icons.Outlined.MusicNote, contentDescription = null)
                else AsyncImage(model = track.artwork, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
    )
}
