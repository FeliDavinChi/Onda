# Onda real playback milestone

## Intent and authorization

Replace the metadata-only foundation with a usable listening app. The existing product brief defines native Android, YouTube Music-compatible catalog access, one global player, and staged social features. On 2 October 2026 the user approved adopting GPL-3.0, including corresponding source with distributed APKs. Routine engineering decisions are delegated by PRODUCT_BRIEF.md; no further product-direction change is proposed here.

## User experience

Home introduces Onda and offers live music discovery. Explore contains cancellable, debounced track search with artwork, artist names, durations, loading/empty/error/retry states. A result starts playback and its visible result list becomes the queue. Each track also offers play-next and add-to-queue. Search results never overwrite a newer query.

A persistent mini player sits above bottom navigation. Tapping it opens the full player with artwork, title/artist, seek slider, elapsed/total time, previous/play-pause/next, shuffle, repeat, and an editable queue. Queue entries have occurrence IDs so duplicates can be moved or removed independently. Playback continues while navigating or using another app. Notification/lockscreen/headset controls share the same player state.

Queue and position survive app/process restart as a bounded snapshot, restored paused. Startup never starts audio. Clear loading and retry states cover provider/network failures. An expired audio URL is refreshed once with bounded recovery, not persisted. Unknown duration disables seeking until duration is known.

Library, accounts, messaging, and social systems retain their roadmap boundaries. This milestone is the player/catalog foundation on which those working features will build.

## Architecture

- Keep existing Kotlin/Compose/Hilt/Room boundaries and min SDK 26, compile/target SDK 35, JDK 17.
- `data:music` supplies `YoutubeMusicSource` through the existing `MusicSource` contract. NewPipeExtractor v0.26.5 is isolated behind an adapter and OkHttp downloader. Provider objects do not escape the module. Metadata-only demo remains test support, not the production binding.
- `domain:music` adds the `PlaybackController` contract and deterministic queue snapshot helpers. `StateFlow<PlaybackState>` is the UI contract. All control methods are asynchronous commands into the service-owned player.
- `core:playback` is an Android library. One `PlaybackService : MediaSessionService` owns ExoPlayer and MediaSession. A singleton `SessionPlaybackController` connects to it. Player timeline is authoritative; the snapshot is persistence only.
- Stream resolution happens off the main thread immediately before reading audio. Validate supported provider IDs and HTTPS stream URLs. Bounded network timeouts, cancellation, no raw stream/token logs, no DRM or protected-content bypass.
- Persist bounded queue metadata, current occurrence, position, repeat and shuffle using app-private storage. Corrupt snapshots restore empty, without crashing. No signed URLs or credentials are stored.
- `app` owns live discovery/search ViewModels and original player Compose UI using existing color/effect/spacing tokens. UI never constructs a player or accesses extraction/network code directly.

## Playback contract

`PlaybackController` exposes `state: StateFlow<PlaybackState>` and commands `play(tracks: List<Track>, startIndex: Int = 0)`, `togglePlayPause()`, `seekTo(positionMs: Long)`, `next()`, `previous()`, `setShuffle(enabled: Boolean)`, `setRepeat(mode: RepeatMode)`, `addToQueue(track: Track)`, `playNext(track: Track)`, `remove(occurrenceId: String)`, `move(occurrenceId: String, toIndex: Int)`, `retry()`, and `clear()`.

Commands run on the player's application looper. Inputs are bounded and invalid queue/index requests cannot crash the service. A later play request supersedes earlier asynchronous resolution. Removing the active occurrence advances coherently; removing the final entry stops playback. Shuffle must not corrupt displayed queue identity.

## Provider boundary and license

Adopt GPL-3.0 for original Onda source, include the full license and third-party notice, and retain corresponding source in GitHub with every distributed APK. NewPipe's own licenses/notices remain applicable. No LastWave source or artwork is imported. NewPipeExtractor is an unofficial provider integration and may be unavailable or restricted by upstream service behavior; show an honest actionable error.

Live catalog/stream verification is a separate check from fixture tests. Do not label a successful compile as proven real playback. Use a publicly available test track for a bounded source/stream request. No device is currently attached, so physical audio, Bluetooth, headset and lockscreen validation must be explicitly reported if still unperformed.

## Verification

Provider tests cover mapping incomplete metadata, malformed/foreign IDs, stream selection/expiry, HTTP failure and cancellation. Search tests cover debounce, blank queries, stale results, error and retry. Queue/snapshot tests cover duplicate tracks, removal/move boundaries, corruption and paused restoration. Playback tests cover late async results and errors without competing player instances. Run the full JVM suite, APK assemble and Android lint, then review the combined diff. Device tests require an attached emulator/device. Update ROADMAP/BUILD_REPORT with evidence and limitations.

## Alternatives considered

1. Independently integrated NewPipe provider + Media3 (selected): follows the intended catalog and playback experience, with GPL adopted explicitly.
2. Local-device music first: simpler provider lifecycle, but changes the initial catalog direction.
3. A licensed commercial catalog: could offer a supported service contract but requires credentials/commercial arrangements and different playback rules.

## Primary references

- https://github.com/TeamNewPipe/NewPipeExtractor/releases/tag/v0.26.5
- https://github.com/TeamNewPipe/NewPipeExtractor/blob/v0.26.5/LICENSE
- https://github.com/TeamNewPipe/NewPipeExtractor/blob/v0.26.5/README.md
- https://developer.android.com/media/media3/session/background-playback
- https://developer.android.com/jetpack/androidx/releases/media3
