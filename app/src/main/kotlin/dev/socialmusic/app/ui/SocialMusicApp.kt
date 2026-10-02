package dev.socialmusic.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import dev.socialmusic.app.R
import dev.socialmusic.common.VisualEffectLevel
import dev.socialmusic.designsystem.*

private enum class Destination(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    Home("home", R.string.home, Icons.Outlined.Home),
    Explore("explore", R.string.explore, Icons.Outlined.Explore),
    Library("library", R.string.library, Icons.Outlined.LibraryMusic),
    Messages("messages", R.string.messages, Icons.AutoMirrored.Outlined.Chat),
    Profile("profile", R.string.profile, Icons.Outlined.Person),
}

@Composable
fun SocialMusicApp(preferences: PreferencesViewModel = hiltViewModel(), playerModel: PlayerViewModel = hiltViewModel()) {
    val player = playerModel.controller
    val playback by player.state.collectAsStateWithLifecycle()
    var playerOpen by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val effect by preferences.effects.collectAsStateWithLifecycle()
    val saveError by preferences.saveError.collectAsStateWithLifecycle()
    val visualEffect = when (effect) {
        VisualEffectLevel.FULL -> EffectLevel.Full
        VisualEffectLevel.REDUCED -> EffectLevel.Reduced
        VisualEffectLevel.MINIMAL -> EffectLevel.Minimal
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
            MiniPlayer(playback, player, visualEffect) { playerOpen = true }
            GlassSurface(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = Spacing.medium, vertical = Spacing.small),
                level = GlassLevel.Elevated, effects = visualEffect,
            ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp, windowInsets = WindowInsets(0)) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = entry?.destination?.route == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = stringResource(destination.label)) },
                            label = { Text(stringResource(destination.label)) },
                            alwaysShowLabel = false,
                        )
                    }
                }
            }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = Destination.Home.route, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(Destination.Home.route) { HomeScreen(player, onExplore = { navController.navigate(Destination.Explore.route) { launchSingleTop = true } }) }
            composable(Destination.Explore.route) { ExploreScreen(player) }
            composable(Destination.Library.route) { EmptyDestination(R.string.library_headline, R.string.library_empty, Icons.Outlined.LibraryMusic) }
            composable(Destination.Messages.route) { EmptyDestination(R.string.messages_headline, R.string.messages_empty, Icons.AutoMirrored.Outlined.Chat) }
            composable(Destination.Profile.route) { ProfileScreen(effect, saveError, preferences::select) }
        }
    }
    if (playerOpen) FullPlayer(playback, player) { playerOpen = false }
}
