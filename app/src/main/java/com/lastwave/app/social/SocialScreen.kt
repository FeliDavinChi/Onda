package com.lastwave.app.social

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lastwave.app.ui.shell.FloatingNavDefaults
import java.util.Locale

@Composable
fun SocialScreen(viewModel: SocialViewModel = hiltViewModel()) {
    val account by viewModel.account.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableIntStateOf(0) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val person = (state.home.following + state.people).firstOrNull { it.id == selectedId }
    BackHandler(selectedId != null) { selectedId = null }
    LaunchedEffect(account?.id) { selectedId = null; section = 0 }
    // Permission-bearing results are short-lived; a refresh invalidates them.
    LaunchedEffect(account?.id) {
        if (account != null) while (true) {
            kotlinx.coroutines.delay(30_000)
            viewModel.refresh()
        }
    }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (person != null) person.displayName else "Your music circle", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            if (account != null) IconButton(onClick = { viewModel.refresh() }, enabled = !state.loading) { Icon(Icons.Default.Refresh, "Refresh friends") }
        }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.message?.let { Text(it, Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        when {
            !viewModel.configured -> Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Music brings people closer", style = MaterialTheme.typography.titleLarge)
                Text("Follow friends, discover what they're playing, and choose whose music taste inspires your recommendations.")
                Text("Accounts aren't available in this build yet. You can keep listening and exploring music.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            account == null -> AccountEntry(state.loading, viewModel)
            !state.homeLoaded -> Column(Modifier.padding(24.dp)) {
                Text("Loading your account…")
                if (!state.loading) {
                    Button({ viewModel.refresh() }) { Text("Try again") }
                    TextButton({ viewModel.signOut() }) { Text("Sign out") }
                }
            }
            state.home.profile == null -> ProfileEntry(state.loading, viewModel)
            person != null -> PersonDetail(person, state.loading, state.home.preferences, { selectedId = null }, viewModel)
            else -> {
                val labels = listOf("Friends", "People", "For you", "Account")
                ScrollableTabRow(section, edgePadding = 16.dp) {
                    labels.forEachIndexed { index, label -> Tab(selected = section == index, onClick = { section = index }, text = { Text(label) }) }
                }
                when (section) {
                    0 -> FriendList(state.home.following, { selectedId = it.id }, viewModel)
                    1 -> PeopleSearch(state.people, state.loading, { selectedId = it.id }, viewModel)
                    2 -> Recommendations(state, viewModel)
                    3 -> AccountPreferences(account!!.email, state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun AccountEntry(loading: Boolean, viewModel: SocialViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    // Passwords are deliberately not persisted into saved-instance state.
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Sign in to Onda", style = MaterialTheme.typography.titleLarge)
        Text("Your music account is separate from your Last.fm or YouTube connection.")
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email))
        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
        Button({ viewModel.signIn(email, password) }, enabled = !loading && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Sign in") }
        OutlinedButton({ viewModel.signUp(email, password) }, enabled = !loading && email.isNotBlank() && password.length >= 8, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        Text("New profiles start with personalized recommendations, listening sharing and taste sharing enabled. You can change these in Account.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProfileEntry(loading: Boolean, viewModel: SocialViewModel) {
    var username by rememberSaveable { mutableStateOf("") }; var name by rememberSaveable { mutableStateOf("") }
    val input = SocialProfileInput(username, name)
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Make yourself easy to find", style = MaterialTheme.typography.titleLarge)
        Text("Personalization and sharing start on. Change them in Account or turn on a private session.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Display name") }, singleLine = true,
            isError = name.isNotEmpty() && input.displayNameError != null,
            supportingText = { if (name.isNotEmpty()) input.displayNameError?.let { Text(it) } })
        OutlinedTextField(username, { username = it.lowercase(Locale.ROOT) }, Modifier.fillMaxWidth(), label = { Text("Username") },
            supportingText = { Text(if (username.isNotEmpty()) input.usernameError ?: "3–24 letters, numbers, or underscores" else "3–24 letters, numbers, or underscores") },
            isError = username.isNotEmpty() && input.usernameError != null, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                autoCorrectEnabled = false,
            ))
        Button({ viewModel.setupProfile(input.username, input.displayName) }, enabled = !loading && input.canSave) { Text(if (loading) "Saving profile…" else "Save profile") }
        TextButton({ viewModel.signOut() }, enabled = !loading) { Text("Sign out") }
    }
}

@Composable
private fun FriendList(people: List<SocialPerson>, onOpen: (SocialPerson) -> Unit, viewModel: SocialViewModel) {
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (people.isEmpty()) item { Text("Find people in the People tab to start your music circle.") }
        items(people, key = { it.id }) { person ->
            ListItem(headlineContent = { Text(person.displayName) }, supportingContent = {
                Text(person.listening?.let { (if (it.live) "Listening now · " else "Last played · ") + it.title + " · " + it.artist } ?: "@${person.username}")
            }, trailingContent = { person.listening?.let { track -> IconButton({ viewModel.play(track) }) { Icon(Icons.Default.PlayArrow, "Play ${track.title}") } } }, modifier = Modifier.clickable { onOpen(person) })
        }
    }
}

@Composable
private fun PeopleSearch(people: List<SocialPerson>, loading: Boolean, onOpen: (SocialPerson) -> Unit, viewModel: SocialViewModel) {
    var query by rememberSaveable { mutableStateOf("") }; var searched by rememberSaveable { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { OutlinedTextField(query, { query = it; searched = false }, Modifier.fillMaxWidth(), label = { Text("Find people") }, singleLine = true) }
        item { Button({ searched = true; viewModel.search(query) }, enabled = query.trim().length >= 2 && !loading) { Text("Search profiles") } }
        if (searched && people.isEmpty() && !loading) item { Text("No profiles found. Try another name.") }
        items(people, key = { it.id }) { person -> ListItem(headlineContent = { Text(person.displayName) }, supportingContent = { Text("@${person.username}") }, modifier = Modifier.clickable { onOpen(person) }) }
    }
}

@Composable
private fun PersonDetail(person: SocialPerson, loading: Boolean, preferences: SocialPreferences, onBack: () -> Unit, viewModel: SocialViewModel) {
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TextButton(onBack) { Text("Back to your circle") }; Text("@${person.username}", style = MaterialTheme.typography.titleMedium) }
        item { Button({ viewModel.follow(person) }, enabled = !loading) { Text(if (person.following) "Following · Unfollow" else "Follow") } }
        person.listening?.let { track -> item { Text(if (track.live) "Listening now" else "Last played", style = MaterialTheme.typography.titleMedium); Text("${track.title} · ${track.artist}"); Button({ viewModel.play(track) }) { Text("Play song") } } }
        item { PreferenceRow("Influence my recommendations", "Blend ${person.displayName}'s shared music taste with yours.", person.influenceEnabled,
            !loading && person.following && person.canInfluence, { viewModel.influence(person, it) }) }
        if (!preferences.personalizationEnabled) item { Text("Turn on personalized recommendations in Account to choose a taste influence.", style = MaterialTheme.typography.bodySmall) }
        else if (!person.canInfluence) item { Text("Follow this person and wait until they allow taste sharing to enable influence.", style = MaterialTheme.typography.bodySmall) }
        if (person.following) item { PreferenceRow("Hide listening activity", "Keep following and recommendation influence unchanged.", person.hiddenActivity, !loading, { viewModel.hide(person, it) }) }
        item { TextButton({ viewModel.block(person); onBack() }, enabled = !loading) { Text("Block this person", color = MaterialTheme.colorScheme.error) } }
    }
}

@Composable
private fun Recommendations(state: SocialUiState, viewModel: SocialViewModel) {
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Your taste, with a little inspiration", style = MaterialTheme.typography.titleLarge); Text("Only the people you choose can influence this feed.") }
        item { Button({ viewModel.recommendations() }, enabled = !state.loading) { Text("Load recommendations") } }
        if (state.recommendations.isEmpty()) item { Text("Enable personalized recommendations in Account, then listen to music or choose an eligible friend's taste.") }
        items(state.recommendations, key = { it.id }) { track ->
            ListItem(headlineContent = { Text(track.title) }, supportingContent = { Text("${track.artist}\n${track.reason}") }, trailingContent = { IconButton({ viewModel.play(track) }) { Icon(Icons.Default.PlayArrow, "Play ${track.title}") } })
            TextButton({ viewModel.moreLike(track) }, enabled = !state.loading && state.home.preferences.personalizationEnabled && !state.home.preferences.privateSession) { Text("More like this") }
        }
    }
}

@Composable
private fun AccountPreferences(email: String, state: SocialUiState, viewModel: SocialViewModel) {
    val value = state.home.preferences
    LazyColumn(contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = FloatingNavDefaults.contentBottomPadding()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(state.home.profile?.displayName.orEmpty(), style = MaterialTheme.typography.titleLarge); Text(email) }
        item { PreferenceRow("Personalized recommendations", "Use your meaningful listening and feedback to learn your taste.", value.personalizationEnabled, !state.loading, { viewModel.preferences(value.copy(personalizationEnabled = it)) }) }
        item { PreferenceRow("Share listening", "Let your followers see your current or last-played song.", value.listeningShared, !state.loading, { viewModel.preferences(value.copy(listeningShared = it)) }) }
        item { PreferenceRow("Share music taste", "Allow followers to explicitly use your music taste in their recommendations.", value.tasteShared, !state.loading, { viewModel.preferences(value.copy(tasteShared = it)) }) }
        item { PreferenceRow("Private session", "Pause listening sharing and new taste-signal collection.", value.privateSession, !state.loading, { viewModel.preferences(value.copy(privateSession = it)) }) }
        if (state.home.blockedPeople.isNotEmpty()) item { Text("Blocked people", style = MaterialTheme.typography.titleMedium) }
        items(state.home.blockedPeople, key = { "blocked-${it.id}" }) { person ->
            ListItem(headlineContent = { Text(person.displayName) }, supportingContent = { Text("@${person.username}") },
                trailingContent = { TextButton({ viewModel.unblock(person) }, enabled = !state.loading) { Text("Unblock") } })
        }
        item { OutlinedButton({ viewModel.signOut() }, enabled = !state.loading) { Text("Sign out of Onda") } }
    }
}

@Composable
private fun PreferenceRow(title: String, description: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Switch(checked, onCheckedChange = onChange, enabled = enabled)
    }
}
