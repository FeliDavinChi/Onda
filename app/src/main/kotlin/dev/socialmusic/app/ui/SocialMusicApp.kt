package dev.socialmusic.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import dev.socialmusic.app.R
import dev.socialmusic.common.VisualEffectLevel
import dev.socialmusic.designsystem.*
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.PlaybackState

private enum class Destination(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    Home("home", R.string.home, Icons.Outlined.Home),
    Explore("explore", R.string.explore, Icons.Outlined.Explore),
    Library("library", R.string.library, Icons.Outlined.LibraryMusic),
    Messages("messages", R.string.messages, Icons.AutoMirrored.Outlined.Chat),
    Profile("profile", R.string.profile, Icons.Outlined.Person),
}

@Composable
fun SocialMusicApp(preferences: PreferencesViewModel = hiltViewModel(), playerModel: PlayerViewModel = hiltViewModel(), listening: ListeningViewModel = hiltViewModel()) {
    val player = playerModel.controller
    val playback by player.state.collectAsStateWithLifecycle()
    var playerOpen by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val effect by preferences.effects.collectAsStateWithLifecycle()
    val saveError by preferences.saveError.collectAsStateWithLifecycle()
    val recent by listening.recent.collectAsStateWithLifecycle()
    val historyError by listening.clearError.collectAsStateWithLifecycle()
    val visualEffect = when (effect) {
        VisualEffectLevel.FULL -> EffectLevel.Full
        VisualEffectLevel.REDUCED -> EffectLevel.Reduced
        VisualEffectLevel.MINIMAL -> EffectLevel.Minimal
    }
    val navigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    OndaScaffold(entry?.destination?.route ?: "home", playback, player, visualEffect, navigate, { playerOpen = true }) { padding ->
        NavHost(navController, startDestination = "home", modifier = Modifier.fillMaxSize().consumeWindowInsets(padding)) {
            composable("home") { HomeScreen(player, { navigate("explore") }, padding, recentlyPlayed = recent, onOpenPlayer = { playerOpen = true }) }
            composable("explore") { ExploreScreen(player, padding) }
            composable("library") { EmptyDestination(R.string.library_headline, R.string.library_empty, Icons.Outlined.LibraryMusic, padding, { navigate("explore") }) }
            composable("messages") { MessagesScreen(padding) { navigate("explore") } }
            composable("profile") { ProfileScreen(effect, saveError, preferences::select, padding, recent.size, historyError, listening::clear) }
        }
    }
    if (playerOpen) FullPlayer(playback, player, visualEffect) { playerOpen = false }
}

/** Production shell also used by host rendering; only the quiet backdrop is captured. */
@Composable
internal fun OndaScaffold(
    route: String,
    playback: PlaybackState,
    player: PlaybackController,
    effects: EffectLevel,
    onNavigate: (String) -> Unit,
    onOpenPlayer: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    ArtworkTheme(playback.currentTrack, effects) { GlassBackdropScope(effects) {
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val imeVisible = WindowInsets.ime.getBottom(density) > 0
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val shortWindow = maxHeight < 500.dp
            val expanded = maxWidth >= 840.dp && maxHeight >= 600.dp
            WaveBackdrop(Modifier.matchParentSize().glassBackdropSource())
            Row(Modifier.fillMaxSize()) {
            if (expanded && !imeVisible) {
                Box(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical))
                    .width(if (density.fontScale >= 1.5f) 136.dp else 104.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    GlassSurface(Modifier.padding(horizontal = 8.dp).fillMaxWidth().testTag("navigation-rail"), GlassLevel.Elevated) {
                        Column(Modifier.fillMaxWidth().padding(8.dp)) {
                            Destination.entries.forEach { destination ->
                                NavigationTab(destination, route == destination.route, Modifier.fillMaxWidth()) { onNavigate(destination.route) }
                            }
                        }
                    }
                }
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                bottomBar = {
                    if (imeVisible) Spacer(Modifier.imePadding()) else
                    Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)).navigationBarsPadding().padding(horizontal = Spacing.medium, vertical = Spacing.small),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        Box(Modifier.widthIn(max = 640.dp).fillMaxWidth()) { MiniPlayer(playback, player, effects, onOpenPlayer) }
                        if (!expanded) GlassSurface(Modifier.widthIn(max = 640.dp).fillMaxWidth(), level = GlassLevel.Elevated) { NavigationDock(route, shortWindow, onNavigate) }
                    }
                },
                content = { padding ->
                    // The route's actual viewport ends above the dock/IME, including bring-into-view.
                    Box(Modifier.fillMaxSize().padding(start = padding.calculateStartPadding(layoutDirection), end = padding.calculateEndPadding(layoutDirection),
                        bottom = padding.calculateBottomPadding()).clipToBounds()) {
                        content(PaddingValues(top = padding.calculateTopPadding()))
                    }
                },
            )
            }
        }
    } }
}

@Composable
private fun NavigationDock(route: String, shortWindow: Boolean, navigate: (String) -> Unit) {
    val largeText = LocalDensity.current.fontScale >= 1.5f
    if (shortWindow && largeText) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Destination.entries.forEach { destination ->
                val requester = remember { BringIntoViewRequester() }
                val active = route == destination.route
                LaunchedEffect(active) { if (active) requester.bringIntoView() }
                NavigationTab(destination, active, Modifier.width(140.dp).bringIntoViewRequester(requester)) { navigate(destination.route) }
            }
        }
        return
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = when {
            !largeText || maxWidth >= 640.dp -> 5
            maxWidth < 340.dp -> 2
            else -> 3
        }
        Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(if (largeText) 4.dp else 0.dp)) {
            Destination.entries.chunked(columns).forEach { destinations ->
                Row(Modifier.fillMaxWidth().padding(horizontal = if (destinations.size < columns) 32.dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    destinations.forEach { destination ->
                        NavigationTab(destination, route == destination.route, Modifier.weight(1f)) { navigate(destination.route) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationTab(destination: Destination, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val label = stringResource(destination.label)
    Column(
        modifier.heightIn(min = 56.dp).clickable(onClick = tactileAction(onClick), role = Role.Tab)
            .semantics { selected = active; contentDescription = label }.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(width = 48.dp, height = 30.dp)
            .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center) {
            Icon(destination.icon, null, Modifier.size(22.dp),
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("nav-label-${destination.route}").clearAndSetSemantics { })
    }
}
