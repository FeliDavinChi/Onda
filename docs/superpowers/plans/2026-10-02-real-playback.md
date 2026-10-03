# Real Playback Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development for independent provider and playback tasks; integrate the UI after their contracts exist. Run meaningful behavior tests before claiming completion.

**Goal:** Make Onda search a live music catalog and play audio through one background-capable player.

**Architecture:** Existing domain models and MusicSource remain the catalog boundary. NewPipe is isolated in data:music; Media3 lives in core:playback; app owns presentation. The service timeline owns the active queue; private snapshots restore paused.

**Tech Stack:** Kotlin, Compose, Hilt, Media3, NewPipeExtractor v0.26.5, OkHttp, coroutines, existing Android SDK 35/JDK 17 toolchain.

**Spec:** docs/superpowers/specs/2026-10-02-real-playback-design.md

## Global Constraints

- min SDK 26, compile/target SDK 35, JDK 17.
- GPL-3.0 approved by user; preserve source/notices alongside APK distribution.
- Exactly one service-owned ExoPlayer; no player in a Composable or Activity.
- Provider JSON/types and ephemeral stream URLs never become durable identity.
- No autonomous audio on startup; snapshot restoration is paused.
- No production backend credentials required for this milestone.
- Preserve cancellation and reject stale asynchronous results.

## Review Focus

- Rapid track changes cannot allow an earlier resolution to replace the latest queue.
- Repeated occurrences of the same track remain independently editable.
- Network/expiry failures leave an explicit recoverable state without retry loops.
- Corrupt snapshots and unknown durations cannot crash or trigger autoplay.
- Audio focus/notification/controller lifecycle uses one session and releases owned resources.

## Task 1: Provider integration

**Files:** data/music build and src/main/test; domain MusicSource error additions if needed (coordinate); root dependency configuration owned by coordinator.

**Interfaces:** Consumes existing MusicSource/Track/MusicId/StreamInfo; produces `YoutubeMusicSource() : MusicSource`. Construction is cheap; initialization/extraction happens off main thread. Expose injectable narrow extraction/transport boundaries for behavior tests. All methods either supply real data or a typed unavailable result, never demo substitution.

- [x] Write behavior tests for mapping, provider-ID rejection, blank query, supported stream selection and error translation. Example: a foreign MusicId must fail before any downloader call; an empty query returns SearchResult without network.
- [x] Run `scripts/gradle-local.ps1 :data:music:test --console=plain`; see failures caused by missing adapter behavior.
- [x] Implement NewPipe downloader, mapping and source. Prefer music search filter. Bound network timeouts and result counts. Preserve cancellation and reject non-HTTPS stream URLs.
- [x] Run provider tests and a separately opt-in live smoke check that searches and resolves a publicly available track; record upstream/network failure honestly.

## Task 2: Playback service and state

**Files:** domain/music playback contract/helpers/tests; new core/playback Android module/main/test; module build configuration owned by coordinator unless agreed.

**Interfaces:** Produce exact PlaybackController contract in spec. `SessionPlaybackController(context)` implements it and `PlaybackService` resolves `MusicSource` via Hilt. Domain/UI share existing PlaybackState, QueueEntry and RepeatMode.

- [x] Write queue/snapshot tests with literal expectations: duplicate IDs have distinct occurrence IDs; move/remove preserves current occurrence; last removal yields empty state; negative positions clamp; corrupt persisted data restores empty/paused.
- [x] Run target tests and observe expected red behavior.
- [x] Implement one MediaSessionService/ExoPlayer, controller StateFlow and command handling, trusted controller validation, audio focus/noisy handling, stream resolution and bounded retry, paused queue persistence.
- [x] Add tests for stale asynchronous resolution/snapshot behavior at owned boundaries and run `:domain:music:test :core:playback:testDebugUnitTest`.

## Task 3: Live discovery/search and player UI

**Files:** app ui/data/di/resources/manifest and UI tests. Consume Tasks 1/2 contracts; do not change provider/playback internals without coordination.

- [x] Write SearchViewModel behavior tests for blank query, 350 ms debounce, stale results, cancellation, failure/retry.
- [x] Implement live Home/Explore with reusable playable track rows and actions. Remove demo-specific production labels. Bind real MusicSource and singleton PlaybackController.
- [x] Add shared mini/full player, seek/progress, controls and queue sheet with occurrence-based remove/move, accessible labels, insets and 200% font-friendly scroll.
- [x] Declare playback service and foreground media permissions. Wire global player outside tab navigation. Update version to 0.2.0 preview/code 2.
- [x] Run app unit tests, assemble and lint; fix failures against the design.

## Task 4: Integrate, verify and deliver

**Files:** LICENSE, THIRD_PARTY_NOTICES.md, README, ROADMAP, BUILD_REPORT and feature files for reviewed corrections.

- [x] Include GPL-3.0 text and dependency attribution; update documentation with actual features and remaining device checks.
- [x] Run `scripts/gradle-local.ps1 test :app:assembleDebug :app:lintDebug --console=plain` and APK signature verification.
- [x] Obtain an independent review of the whole branch including the Review Focus list. Resolve important findings and rerun covering checks.
- [x] Verify live provider behavior separately from fixture tests and confirm source/asset mapping for the APK.
- [ ] Commit reviewed work, push a feature branch and create a reviewable PR. Prior public repo authorization applies; avoid overwriting the existing v0.1.0 release. Provide the built APK and accurate validation status.
