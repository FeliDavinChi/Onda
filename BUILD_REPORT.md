# Onda verification report

## 0.2.0 real playback preview — 2 October 2026

Implemented live song discovery/search through NewPipeExtractor v0.26.5, a service-owned Media3 ExoPlayer/session, app controller, mini/full player, seeking, shuffle/repeat, occurrence-based queue edits, bounded retry and private ordered snapshots restored paused. No playback URL is persisted. Original code now uses GPL-3.0; source tags, dependency notices and packaged license texts accompany APK distribution.

Host verification ran `scripts/gradle-local.ps1 test :app:assembleDebug :app:lintDebug --console=plain` successfully. JUnit reports contain 115 executions: 113 passed, zero failures/errors, two intentionally skipped live checks. This covers app debug/release/staging and playback debug/release variants. Provider tests cover mapping/stream delivery/HTTP cancellation; search tests cover debounce, stale results and retry; queue/snapshot tests cover duplicates, paused restore, corruption/byte bounds and real AtomicFile ordering on Robolectric SDK28. Actual Media3 unprepared timeline tests verify active occurrence/position through move/remove. These are host checks, not speaker/device tests.

Final build: 406 tasks, 75 executed and 331 up-to-date, `BUILD SUCCESSFUL`. Lint: zero errors, 48 dependency/AGP update warnings, with no suppressed findings. APK: application `dev.socialmusic.app.debug`, label Onda, version 0.2.0/code 2, min SDK26/target SDK35, 28,147,952 bytes. `apksigner verify --verbose --print-certs` passed (v2 signature, development debug certificate). SHA-256: `9b1a8c7606a08345cbfce4ca72ead7a215d37c1473785a8b723e8939dadb20b8`. The APK includes GPL/Apache/Rhino/jsoup/protobuf license texts and third-party notices under `assets/licenses/`.

Separately, opt-in live checks passed: music search returned 20 tracks, and a publicly available track resolved to HTTPS audio/webm and returned 1,024 audio bytes with HTTP 206. URLs/tokens were not recorded. Normal CI skips live checks to avoid dependence on upstream availability. The adapter is unofficial and availability can change.

Independent read-only reviews covered provider, playback and app UI. Corrected DASH delivery acceptance, discarded parsing errors, pending-restore destruction, API33-only snapshot reading, cross-service writer races, ignored session results, skip/retry after errors, hidden connection failures, buffering control labels and accessibility/insets. Windows Gradle workspace rename errors were intermittent; canonical portable-tool cache paths and a subsequent build completed without clearing shared caches.

No device or emulator is attached. Physical audio, focus interruptions, background/lockscreen/headset/Bluetooth controls, process-kill behavior, rendered 200% fonts/TalkBack and frame/battery checks remain unperformed. Backend authentication, likes/library/history, album/artist/playlist details, messaging and social features remain roadmap work. This preview does not close those gates.

The repository is public at https://github.com/FeliDavinChi/Onda. The previous v0.1.0 APK remains in Releases. The historical foundation report below describes the earlier local state; its statements about no remote/provider/license are superseded by this preview.

## Historical foundation baseline

Branding update, 2 October 2026: the app is named **Onda**. Updated the visible app-name resource, Gradle project name and product documentation. Debug assemble and lint passed; Android `aapt dump badging` confirmed the APK application label is `Onda`. No behavior changed, so the existing behavioral test baseline below was not rerun for this rename.

Date: 2 October 2026 (Asia/Kolkata). Scope: Phase 0 reconnaissance and the first Phase 1 foundation increment. This is a local Git repository on `codex/foundation`, with no remote configured or publication performed.

## Implemented

- Eight Gradle modules: app, core/common, core/model, core/database, core/network, core/designsystem, domain/music, data/music.
- Shared music, social, recommendation and playback-state models; provider-namespaced validated/encoded music identity.
- Replaceable MusicSource and original deterministic metadata-only DemoMusicSource, with typed missing/unavailable errors.
- Hilt composition, Room public metadata cache with exported v1 schema, bounded queries, atomic pruning and unique provider identity.
- Optional Supabase Auth/Postgrest/Realtime factory; validated HTTPS/public publishable-key configuration; no app auth/config wiring or credentials embedded.
- Lifecycle-aware catalog ViewModel, loading/empty/error/retry and cancellation handling; persisted visual-effect preference in DataStore.
- Five-destination Compose shell, dark/light tokens and restrained tint/elevation glass; explicit local demo and deferred-feature empty states.
- TalkBack names for navigation, scalable scroll layouts, and explicit cloud/device-transfer backup exclusions.
- Pinned/checksummed Gradle tooling, local portable tools, atomic verified downloads, CI workflow and product/system documents.

## Executed verification

```powershell
.\scripts\test-download.ps1
.\scripts\gradle-local.ps1 test :app:assembleDebug :app:lintDebug --console=plain
```

Both completed successfully. Final Android verification: **BUILD SUCCESSFUL**, 307 tasks, 83 executed and 224 up-to-date after the final source changes. The previous full build also succeeded; final verification repeated because theme and backup configuration changed.

JUnit reports show 39 successful executions, zero failures/errors: 9 backend configuration tests; 5 music identity tests; 3 model serialization/playback-state tests; 10 source tests; and 4 ViewModel tests executed in each of debug/release/staging. That is 31 distinct behavioral tests. The download helper separately passed fixture checks for checksum validation, atomic promotion, preservation of a good file, partial cleanup and recovery.

Red/green evidence: initial identity/config/source contracts failed their behavioral tests; ViewModel's initial refresh contract failed all four tests; malformed Unicode acceptance failed its regression test before rejection was implemented. Subsequent final reports pass. The Windows wrapper propagates failures (failed test command returned exit code 1).

Android lint completed with zero errors and **39 warnings**, all dependency/AGP update notices (6 AndroidGradlePluginVersion, 33 GradleDependency). They are retained, not suppressed. The initial missing data-extraction-rules warning was fixed. The deliberately conservative pinned SDK 35/AGP 8.9 baseline must be reevaluated with dependency security and target-SDK requirements before public release.

Debug packaging emitted a native-symbol stripping notice for dependency libraries; the debug APK packages those libraries unchanged. No NDK was installed merely to strip debug artifacts. Release binary size/native tooling remain hardening checks.

Outputs: `app/build/outputs/apk/debug/app-debug.apk`; HTML lint report at `app/build/reports/lint-results-debug.html`; JUnit reports under each module's `build/test-results`; Room schema at `core/database/schemas/dev.socialmusic.database.MusicDatabase/1.json`. Local build logs are in ignored `.tools/`; tools/build artifacts are not committed. The SDK/JDK/Gradle downloads were checked against their published hashes.

## Review

Independent read-only review found no critical issues. Fixed its important navigation accessibility issue and minor configuration-documentation, malformed Unicode identity, and interrupted-download findings. No copied reference code/assets or application secrets were found in the source review. The parent VoiceNotes application and preexisting edits were preserved.

## Unperformed checks and next gate

No emulator/device launch, screenshot QA, TalkBack execution, large-font UI inspection, frame/battery measurement, Room instrumentation or real backup/restore test was run. No Supabase schema/policies were deployed or tested, and the CI workflow was created but not run remotely. Device UI checks remain necessary before declaring UI acceptance complete.

Real audio/provider integration, the single MediaSessionService/player, queue and mini/full player are Phase 2. The demo deliberately cannot stream. Auth, real chat/social activity, recommendation ranking and RLS enforcement are future implementation work; architecture documents do not imply those features exist. Playback stability/device verification is the gate before advancing to discovery and social milestones.

## Project relocation

On 2 October 2026, the repository moved from `C:/Users/laksh/OneDrive/Documents/VoiceNotes/social-music` to the existing Onda project folder at `C:/Users/laksh/OneDrive/Documents/ChatGPT/Onda`. Both original commits, portable build tools and APK were preserved. The previous empty destination Git metadata is backed up inside ignored `.tools/transfer-empty-onda-repository.git`. The ignored SDK path was updated for the new location. The old project folder was removed by the transfer; the VoiceNotes application was not changed.

Verification from the new folder passed: 39 test executions with zero failures/errors, debug APK assembly, and Android lint with zero errors and the same 39 dependency-update warnings. The APK application label remains Onda.
