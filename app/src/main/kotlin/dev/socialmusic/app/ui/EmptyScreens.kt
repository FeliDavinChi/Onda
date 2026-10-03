package dev.socialmusic.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.socialmusic.app.R
import dev.socialmusic.common.VisualEffectLevel
import dev.socialmusic.designsystem.*

@Composable
fun EmptyDestination(@StringRes title: Int, @StringRes description: Int, icon: ImageVector, padding: PaddingValues = PaddingValues(), onExplore: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp,
        top = padding.calculateTopPadding() + 48.dp, bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary) }
        item { Text(stringResource(title), style = MaterialTheme.typography.headlineLarge) }
        item { Text(stringResource(description), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { FilledTonalButton(onClick = onExplore, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.find_music)) } }
    }
}

@Composable
fun MessagesScreen(padding: PaddingValues = PaddingValues(), onExplore: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp,
        top = padding.calculateTopPadding() + 24.dp, bottom = padding.calculateBottomPadding() + 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { Text(stringResource(R.string.messages), style = MaterialTheme.typography.headlineLarge) }
        item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
        item {
            Spacer(Modifier.height(40.dp))
            Text(stringResource(R.string.messages_intro), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.messages_detail), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onExplore, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.find_music)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(effect: VisualEffectLevel, saveError: Boolean, onSelect: (VisualEffectLevel) -> Unit, padding: PaddingValues = PaddingValues(),
    historyCount: Int = 0, historyError: Boolean = false, onClearHistory: () -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    var confirmClear by remember { mutableStateOf(false) }
    if (confirmClear) BasicAlertDialog(onDismissRequest = { confirmClear = false }) {
        WindowGlassSurface {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.clear_history), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.clear_history_detail), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = { confirmClear = false; onClearHistory() }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.clear_history)) }
                TextButton(onClick = { confirmClear = false }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancel)) }
            }
        }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp,
        top = padding.calculateTopPadding() + 24.dp, bottom = padding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Icon(Icons.Outlined.Person, null, Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).padding(16.dp),
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.profile_headline), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.profile_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.visual_effects), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.effects_description), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.selectableGroup()) {
                    VisualEffectLevel.entries.forEach { level ->
                        val label = when (level) { VisualEffectLevel.FULL -> R.string.effect_full; VisualEffectLevel.REDUCED -> R.string.effect_reduced; VisualEffectLevel.MINIMAL -> R.string.effect_minimal }
                        val detail = when (level) { VisualEffectLevel.FULL -> R.string.effects_full_description; VisualEffectLevel.REDUCED -> R.string.effects_reduced_description; VisualEffectLevel.MINIMAL -> R.string.effects_minimal_description }
                        Row(Modifier.fillMaxWidth().selectable(selected = level == effect, onClick = { onSelect(level) }, role = Role.RadioButton)
                            .padding(horizontal = 12.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RadioButton(selected = level == effect, onClick = null)
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(label), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(detail), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
        if (saveError) item { Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error) }
        item {
            Text(stringResource(R.string.listening_history), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.history_privacy), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { confirmClear = true }, enabled = historyCount > 0) { Text(stringResource(R.string.clear_history)) }
            if (historyError) Text(stringResource(R.string.history_error), color = MaterialTheme.colorScheme.error)
        }
        item {
            Text(stringResource(R.string.open_source_license), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { uriHandler.openUri("https://github.com/FeliDavinChi/Onda/tree/v0.4.0") }) { Text(stringResource(R.string.view_source_license)) }
        }
    }
}
