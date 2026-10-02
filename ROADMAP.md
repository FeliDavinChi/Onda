# Implementation roadmap

Initial task: reconnaissance -> architecture documents -> begin Phase 1. Status is evidence-based; planned product features are not marked complete because interfaces exist.

Current status: Phase 1 foundation is host-verified. The Phase 2 playback implementation and song-search portion of Phase 3 are implemented: NewPipe provider, one service-owned Media3 player, queue, mini/full controls, debounced search and paused restoration. Fixture tests, Robolectric timeline/storage checks, live search/audio-byte requests, debug assembly and lint pass. Phase 2's physical playback/background/focus/headset gate remains open; album/artist details and backend/social/library features remain future work. [BUILD_REPORT.md](BUILD_REPORT.md) records the evidence and limits.

| Phase | Deliverable | Gate before progression |
| --- | --- | --- |
| 0 | Local/reference reconnaissance; five system documents | Existing implementation identified; target repository resolved |
| 1 | Kotlin modules/models/source contract, Hilt, Room cache, network factory, DataStore effects, Compose navigation/theme | JVM tests, debug assemble and lint; document device checks separately |
| 2 | Reviewed real provider, one MediaSessionService/player, queue, mini/full controls | Queue/state tests; real audio; background/lockscreen/headset/focus/expiry/device verification |
| 3 | Debounced cancellable unified search, album/artist/track, Explore/Home | Stale results never replace current query; loading/empty/error/retry; provider contract tests |
| 4 | Library likes/playlists/history/offline cache | Ordering/retry/idempotency/migration tests; account partitioning |
| 5 | Supabase email auth, optional Google, username/profile/onboarding | Session/refresh/logout/deep-link flows verified; no secret keys; server auth/schema tests |
| 6 | Follows/block/privacy/activity/throttled now-playing | RLS visibility matrix; privacy revocation/cache purge; presence expiry |
| 7 | One-to-one messages/outbox/Realtime/typing/reads/cursor pagination | Outsider/forged membership tests; reconnect/dedup/retry/order/device checks |
| 8 | Typed music attachments/internal share/external chooser | Play from conversation uses the sole player; expired/missing attachments; polished accessible cards |
| 9 | Versioned signals/taste/hybrid/diversity/exploration/explanations | Deterministic ranking/privacy/event tests; reset/opt-out/revocation |
| 10 | Lyrics/palette cache/glass refinement/transitions/haptics | Contrast/font/TalkBack/reduced effects/frame measurements |
| 11 | Security/offline/performance/battery/crash/release hardening | Adversarial RLS, device tests, credential/dependency/license review and release checklist |
| 12 | Shared listening/mixes/collaboration/groups/ML | Core playback/messaging stable and supporting consented data exists |

## Phase 1 execution scope

1. Initialize separate Git repository, ignored local tool paths and pinned Gradle wrapper. Preserve parent user edits.
2. Create JVM `core:model`, `core:common`, `domain:music`, `data:music`; write behavioral tests for namespaced identity, malformed state/config and deterministic source search/missing-stream errors.
3. Establish Android modules `core:database`, `core:network`, `core:designsystem`, `app`, with centralized stable dependencies, SDK 35/min SDK 26 and JDK 17. Initial version baseline is conservative; reassess target/dependencies before public release.
4. Add a bounded Room metadata cache and exported schema, fail-closed optional Supabase client provisioning, Hilt bindings, and effect preferences in DataStore. Do not install private-data tables without auth boundaries.
5. Build a Compose shell with five navigation destinations, lifecycle-aware ViewModel catalog state, honest local demo metadata and persistent effect preference. No simulated playback or messaging success. Empty destinations remain empty until their phase.
6. Run JVM tests, Android assemble and lint; fix failures. Record any unperformed instrumentation/device/server checks explicitly. Update implementation status and build report from actual results.

## Definition of done

Every feature requires appropriate architecture, working implementation, compilation, meaningful passing tests, loading/error/empty states, accessible consistent UI, backend privacy/security, reasonable performance and updated documentation. Real-device and backend policy evidence are needed for those respective claims. A mock provider does not close Phase 2. Creating a Supabase client does not close Phase 5. SQL designs do not close RLS gates.

## External prerequisites

Android SDK license acceptance/build tooling; Supabase project URL and public publishable key when auth work starts; provider dependency and endpoint licensing review before use; production app ID/domain/signing and distribution license before release. Avoid creating accounts, deploying database changes or publishing without the user's relevant authorization.
