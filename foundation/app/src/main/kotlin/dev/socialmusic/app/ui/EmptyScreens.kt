package dev.socialmusic.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.socialmusic.app.R
import dev.socialmusic.common.VisualEffectLevel
import dev.socialmusic.designsystem.Spacing

@Composable
fun EmptyDestination(@StringRes title: Int, @StringRes description: Int, icon: ImageVector) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        item { Spacer(Modifier.height(Spacing.section)); Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) }
        item { Text(stringResource(title), style = MaterialTheme.typography.headlineLarge) }
        item { Text(stringResource(description), color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun ProfileScreen(effect: VisualEffectLevel, saveError: Boolean, onSelect: (VisualEffectLevel) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(Spacing.large), verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        item { Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) }
        item { Text(stringResource(R.string.profile_headline), style = MaterialTheme.typography.headlineLarge) }
        item { Text(stringResource(R.string.profile_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            Spacer(Modifier.height(Spacing.section))
            Text(stringResource(R.string.visual_effects), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.effects_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(VisualEffectLevel.entries.size) { index ->
            val level = VisualEffectLevel.entries[index]
            val label = when (level) {
                VisualEffectLevel.FULL -> R.string.effect_full
                VisualEffectLevel.REDUCED -> R.string.effect_reduced
                VisualEffectLevel.MINIMAL -> R.string.effect_minimal
            }
            FilterChip(
                selected = level == effect, onClick = { onSelect(level) },
                label = { Text(stringResource(label)) }, modifier = Modifier.heightIn(min = 48.dp),
            )
        }
        if (saveError) item { Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error) }
    }
}
