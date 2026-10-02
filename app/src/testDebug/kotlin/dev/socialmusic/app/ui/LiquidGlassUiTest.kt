package dev.socialmusic.app.ui

import android.app.Application
import android.app.ActivityManager
import android.content.Context
import android.graphics.*
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.socialmusic.common.LoadState
import dev.socialmusic.designsystem.*
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.test.FakeImageLoaderEngine

/** Production UI with offline fixtures. PNGs are review artifacts, not snapshot goldens. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w412dp-h892dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LiquidGlassUiTest {
    @get:Rule val ui = createAndroidComposeRule<ComponentActivity>()
    private val tracks by lazy { fixtures() }
    private val playback by lazy { PlaybackState(tracks.mapIndexed { i, t -> QueueEntry("occurrence-$i", t) }, 0,
        status = PlaybackStatus.PAUSED, positionMs = 61_000, durationMs = 224_000) }
    private val player by lazy { RecordingPlayer(playback) }

    @Test fun homeDark() {
        renderHome(true, EffectLevel.Full); capture("home-dark")
        val bounds = ui.onNodeWithText("Onda").fetchSemanticsNode().boundsInRoot
        val image = BitmapFactory.decodeFile("build/reports/ui/home-dark.png")
        var brightPixels = 0
        for (y in bounds.top.toInt() until bounds.bottom.toInt()) for (x in bounds.left.toInt() until bounds.right.toInt()) {
            val color = image.getPixel(x, y)
            if (android.graphics.Color.red(color) > 180 && android.graphics.Color.green(color) > 180 && android.graphics.Color.blue(color) > 180) brightPixels++
        }
        assertTrue("Dark headings must use a readable light foreground", brightPixels > 20)
    }
    @Test fun homeLight() { renderHome(false, EffectLevel.Full); capture("home-light") }
    @Test fun homeMinimalLargeTextKeepsActionsReachable() {
        renderHome(true, EffectLevel.Minimal, 2f)
        ui.onNodeWithText("Search songs and artists").performScrollTo().assertIsDisplayed()
        capture("home-large-minimal")
        listOf("home", "explore", "library", "messages", "profile").forEach { ui.onNodeWithTag("nav-label-$it", useUnmergedTree = true).assertIsDisplayed() }
        ui.onNodeWithContentDescription("Explore").performClick()
        assertEquals("explore", player.lastRoute)
    }
    @Test @Config(qualifiers = "w320dp-h740dp-mdpi") fun narrowLargeTextKeepsVisibleDestinationNames() {
        renderHome(true, EffectLevel.Minimal, 2f)
        listOf("home", "explore", "library", "messages", "profile").forEach { ui.onNodeWithTag("nav-label-$it", useUnmergedTree = true).assertIsDisplayed() }
        ui.onNodeWithText("Search songs and artists").performScrollTo().assertIsDisplayed()
        val search = ui.onNodeWithText("Search songs and artists").fetchSemanticsNode().boundsInRoot
        val dock = ui.onNodeWithTag("mini-player").fetchSemanticsNode().boundsInRoot
        assertTrue("Search stays above floating controls", search.bottom <= dock.top)
        capture("home-narrow-large")
    }
    @Test fun miniPlayerOpensAndBufferingKeepsPauseIntent() {
        var opened = false
        ui.setContent { SocialMusicTheme { GlassBackdropScope(EffectLevel.Full) {
            MiniPlayer(playback.copy(status = PlaybackStatus.BUFFERING, buffering = true, playWhenReady = true), player, EffectLevel.Full) { opened = true }
        } } }
        ui.onNode(hasClickAction() and hasText(tracks.first().title)).performClick()
        assertTrue(opened)
        ui.onNodeWithContentDescription("Pause").performClick(); assertEquals("toggle", player.lastCommand)
        ui.onNodeWithContentDescription("Next track").performClick(); assertEquals("next", player.lastCommand)
    }
    @Test @Config(qualifiers = "w360dp-h360dp-mdpi") fun shortWindowAtLargeTextKeepsContentUsable() {
        renderHome(true, EffectLevel.Minimal, 2f)
        listOf("home", "explore", "library", "messages", "profile").forEach { ui.onNodeWithTag("nav-label-$it", useUnmergedTree = true).assertIsDisplayed() }
        ui.onNodeWithText("Search songs and artists").performScrollTo().assertIsDisplayed()
        val search = ui.onNodeWithText("Search songs and artists").fetchSemanticsNode().boundsInRoot
        val dock = ui.onNodeWithTag("mini-player").fetchSemanticsNode().boundsInRoot
        assertTrue("Short windows keep a usable route viewport", dock.top >= 100f)
        assertTrue("Search stays above floating controls in split screen", search.bottom <= dock.top)
        capture("home-short-large")
    }
    @Test fun searchClearAndQueueMenuRetainActions() {
        var query by mutableStateOf("Night")
        ui.setContent { SocialMusicTheme(dark = true) { OndaScaffold("explore", playback, player, EffectLevel.Full, {}, {}) { padding ->
            ExploreContent(SearchUiState(query, LoadState.Ready(tracks)), playback, player, { query = it }, {}, padding)
        } } }
        capture("search-dark")
        ui.onNodeWithContentDescription("Search songs and artists").assert(hasSetTextAction())
        ui.onNodeWithContentDescription("Clear search").performClick()
        ui.runOnIdle { assertEquals("", query) }
        ui.onNodeWithContentDescription("Actions for ${tracks.first().title}").performClick()
        ui.onNodeWithText("Play next").performClick(); assertEquals(tracks.first().id, player.nextTrack?.id)
    }
    @Test fun fullPlayerTransportAndQueueRemainReachable() {
        renderPlayer(true, EffectLevel.Full); capture("player-dark")
        ui.onNodeWithContentDescription("Shuffle off").performScrollTo().performClick(); assertTrue(player.shuffleValue)
        ui.onNodeWithContentDescription("Repeat off").performClick(); assertEquals(RepeatMode.ALL, player.repeatValue)
        ui.onNodeWithContentDescription("Play").performClick(); assertEquals("toggle", player.lastCommand)
        ui.onNodeWithContentDescription("Seek position").performSemanticsAction(SemanticsActions.SetProgress) { it(100_000f) }
        assertEquals(100_000L, player.seekPosition)
        ui.onNodeWithContentDescription("Open queue").performClick()
        ui.onNodeWithContentDescription("Remove ${tracks.first().title} from queue").performClick(); assertEquals("occurrence-0", player.removed)
    }
    @Test fun fullPlayerLight() { renderPlayer(false, EffectLevel.Full); capture("player-light") }
    @Test fun fullPlayerLargeTextScrollsToTransport() {
        renderPlayer(true, EffectLevel.Minimal, 2f)
        ui.onNodeWithContentDescription("Shuffle off").performScrollTo().assertIsDisplayed()
        ui.onNodeWithContentDescription("Next track").assertIsDisplayed().performClick(); assertEquals("next", player.lastCommand)
        capture("player-large-minimal")
    }
    @Test fun searchEmptyAndFailureDirectUser() {
        var state by mutableStateOf(SearchUiState())
        var retried = false
        ui.setContent { SocialMusicTheme { OndaScaffold("explore", PlaybackState(), player, EffectLevel.Full, {}, {}) { padding ->
            ExploreContent(state, PlaybackState(), player, { state = SearchUiState(it) }, { retried = true }, padding)
        } } }
        capture("search-empty")
        ui.onNodeWithText("Jazz").performClick()
        ui.runOnIdle { assertEquals("Jazz", state.query); state = SearchUiState("Jazz", LoadState.Failed) }
        ui.onNodeWithText("Try again").performClick(); assertTrue(retried)
    }
    @Test fun imeReservesSpaceAndHidesDockWhileSearching() {
        ui.setContent { SocialMusicTheme { OndaScaffold("explore", playback, player, EffectLevel.Full, {}, {}) { padding ->
            ExploreContent(SearchUiState("Night", LoadState.Ready(tracks)), playback, player, {}, {}, padding)
        } } }
        ui.runOnUiThread {
            val insets = WindowInsetsCompat.Builder().setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 300))
                .setVisible(WindowInsetsCompat.Type.ime(), true).build()
            ViewCompat.dispatchApplyWindowInsets(ui.activity.findViewById<View>(android.R.id.content), insets)
        }
        ui.onNodeWithContentDescription("Home").assertDoesNotExist()
        ui.onNodeWithTag("search-results").performScrollToNode(hasText(tracks.last().title))
        ui.onNodeWithText(tracks.last().title).assertIsDisplayed()
        val bounds = ui.onNodeWithText(tracks.last().title).fetchSemanticsNode().boundsInRoot
        assertTrue("Last result stays above the keyboard", bounds.bottom <= 892 - 300)
        capture("search-ime")
    }
    @Test @Config(sdk = [28]) fun olderAndroidUsesReadableFallback() {
        renderHome(true, EffectLevel.Full)
        ui.onNodeWithContentDescription("Home").assertIsSelected()
        ui.onNodeWithText("Search songs and artists").assertIsDisplayed(); capture("home-api28-fallback")
    }
    @Test fun lowRamGlassUsesOpaqueMaterial() {
        val manager = RuntimeEnvironment.getApplication().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        shadowOf(manager).setIsLowRamDevice(true)
        renderMaterial(EffectLevel.Full, GlassLevel.Elevated)
        capture("material-low-ram")
        assertEquals(0xFF1B222B.toInt(), materialPixel("material-low-ram"))
    }
    @Test fun noneMaterialStaysOpaqueInReducedMode() {
        renderMaterial(EffectLevel.Reduced, GlassLevel.None)
        capture("material-none-reduced")
        assertEquals(0xFF1B222B.toInt(), materialPixel("material-none-reduced"))
    }
    @Test fun fullGlassActuallySamplesBackdrop() {
        val manager = RuntimeEnvironment.getApplication().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        shadowOf(manager).setIsLowRamDevice(false)
        renderMaterial(EffectLevel.Full, GlassLevel.Elevated)
        capture("material-full")
        val pixel = materialPixel("material-full")
        // A red source must affect the material; foreground-only blur or solid tint would not.
        assertTrue(android.graphics.Color.red(pixel) > android.graphics.Color.red(0xFF1B222B.toInt()) + 20)
    }
    private fun renderMaterial(effects: EffectLevel, level: GlassLevel) {
        ui.setContent { SocialMusicTheme(dark = true) { GlassBackdropScope(effects) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.matchParentSize().glassBackdropSource().background(Color.Red))
                GlassSurface(Modifier.fillMaxSize(), level) { }
            }
        } } }
    }
    private fun materialPixel(name: String): Int = BitmapFactory.decodeFile("build/reports/ui/$name.png").let { it.getPixel(it.width / 2, it.height / 2) }
    private fun renderHome(dark: Boolean, effects: EffectLevel, fontScale: Float = 1f) {
        RuntimeEnvironment.setFontScale(fontScale)
        ui.setContent { Themed(dark, fontScale) {
            OndaScaffold("home", playback, player, effects, { player.lastRoute = it }, {}) { padding ->
                HomeContent(LoadState.Ready(tracks), playback, player, {}, {}, padding)
            }
        } }
    }
    private fun renderPlayer(dark: Boolean, effects: EffectLevel, fontScale: Float = 1f) {
        RuntimeEnvironment.setFontScale(fontScale)
        ui.setContent { Themed(dark, fontScale) { FullPlayer(playback, player, effects) {} } }
    }
    @Composable private fun Themed(dark: Boolean, fontScale: Float, content: @Composable () -> Unit) {
        SocialMusicTheme(dark, content)
    }
    @OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)
    private fun capture(name: String) {
        ui.waitForIdle(); ui.mainClock.advanceTimeBy(500); ui.waitForIdle()
        val root = if (name.startsWith("player")) ui.onNode(isDialog()) else ui.onRoot()
        val dir = File("build/reports/ui").apply { mkdirs() }
        val file = File(dir, "$name.png")
        root.captureRoboImage(file.absolutePath, roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record))
        assertTrue(file.isFile && file.length() > 1000)
    }
    @OptIn(coil3.annotation.ExperimentalCoilApi::class, coil3.annotation.DelicateCoilApi::class)
    private fun fixtures(): List<Track> {
        val dir = File("build/ui-fixtures").apply { mkdirs() }
        val titles = listOf("Midnight motion", "Ocean avenue", "Soft focus", "After the rain", "Moonlight drive", "Slow mornings")
        val artists = listOf("The North Room", "Mira", "Night Garden", "Luna", "Blue Hours", "Daybreak")
        val palettes = listOf(0xFFBE754D.toInt() to 0xFF302044.toInt(), 0xFF236C78.toInt() to 0xFF94C9C8.toInt(),
            0xFF827ABC.toInt() to 0xFFC99D88.toInt(), 0xFF506B45.toInt() to 0xFFA8BD91.toInt())
        val engine = FakeImageLoaderEngine.Builder()
        val result = titles.mapIndexed { i, title ->
            val (a, b) = palettes[i % palettes.size]
            val bitmap = Bitmap.createBitmap(420, 420, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = LinearGradient(0f, 0f, 420f, 420f, a, b, Shader.TileMode.CLAMP) }
            canvas.drawRect(0f, 0f, 420f, 420f, paint)
            paint.shader = null; paint.color = 0x50FFFFFF; paint.strokeWidth = 2f; paint.style = Paint.Style.STROKE
            repeat(18) { line -> canvas.drawCircle(210f + i * 16, 210f, 30f + line * 13, paint) }
            val file = File(dir, "cover-$i.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val artwork = "https://fixtures.invalid/cover-$i.png"
            engine.intercept(artwork, bitmap.asImage())
            Track(MusicId("fixture", "track-$i"), title, listOf(ArtistRef(MusicId("fixture", "artist-$i"), artists[i])), artwork = artwork, durationMs = 224_000)
        }
        SingletonImageLoader.setUnsafe(ImageLoader.Builder(RuntimeEnvironment.getApplication()).components { add(engine.build()) }.build())
        return result
    }
}

private class RecordingPlayer(initial: PlaybackState) : PlaybackController {
    override val state = MutableStateFlow(initial)
    var lastCommand = ""; var lastRoute = ""; var nextTrack: Track? = null
    var shuffleValue = false; var repeatValue = RepeatMode.OFF; var seekPosition = -1L; var removed = ""
    override fun play(tracks: List<Track>, startIndex: Int) { lastCommand = "play:$startIndex" }
    override fun togglePlayPause() { lastCommand = "toggle" }
    override fun seekTo(positionMs: Long) { seekPosition = positionMs }
    override fun next() { lastCommand = "next" }
    override fun previous() { lastCommand = "previous" }
    override fun setShuffle(enabled: Boolean) { shuffleValue = enabled }
    override fun setRepeat(mode: RepeatMode) { repeatValue = mode }
    override fun addToQueue(track: Track) { lastCommand = "add" }
    override fun playNext(track: Track) { nextTrack = track }
    override fun remove(occurrenceId: String) { removed = occurrenceId }
    override fun move(occurrenceId: String, toIndex: Int) { lastCommand = "move" }
    override fun retry() { lastCommand = "retry" }
    override fun clear() { lastCommand = "clear" }
}
