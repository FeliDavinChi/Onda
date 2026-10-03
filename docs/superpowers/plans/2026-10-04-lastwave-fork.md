# LastWave-based Onda implementation plan

> Execute in this chat using superpowers:executing-plans. The user's instruction to continue building authorizes implementation of the reviewed direction.

**Goal:** Make LastWave the active Onda Android app and add a Supabase-backed social boundary with explicitly selected shared-taste influence.

**Architecture:** Preserve the existing foundation under `foundation/`; import pinned LastWave source into the active root build. Its single music player remains authoritative. Add Onda social repositories/ViewModels and a versioned server recommendation contract; Supabase owns data and authorization, and later Python ranking implements that same contract.

**Tech stack:** Kotlin, Compose, Hilt, Room, Media3, OkHttp, Supabase/PostgreSQL, Deno Edge Functions for bounded V1 ranking.

**Spec:** `docs/superpowers/specs/2026-10-04-lastwave-fork-design.md`

## Global constraints

- Preserve upstream GPL-3.0 and copyright notices; record source commit `3156a434d7e798a02df7e5fd47ae13f10a9c983a`.
- Install application ID `dev.socialmusic.app`; retain `com.lastwave.app` namespace/JNI bindings initially.
- No upstream APK updates, private/service-role client credentials, fabricated remote success, or second player.
- Follow and listening sharing are separate from per-person shared-taste influence; all broadcasting/influence defaults off.
- Retain source permission and receiver selection checks when serving recommendations.
- Work in the current checkout, preserving all existing document changes.

## Review focus

- A fork must not install or update as LastWave: check manifest identity/authorities and update manager.
- A missing backend must leave guest music usable and expose an honest social setup state.
- Sessions/account data must not leak through backup, logs, or sign-out.
- A follow must not imply taste influence, and blocking/revocation must defeat stale caches.
- Shared music must reach the existing player without transporting stream URLs or private local paths.

## Task 1: Import and establish a reproducible music baseline

**Files:** preserve existing `app/`, `core/`, `data/`, `domain/`, and root build inputs under `foundation/`; import upstream `app/`, `audio/`, `gradle/`, build inputs, and native-header tool; create `UPSTREAM.md` and preserve `LICENSE`.

**Consumes:** verified source checkout and current portable JDK/SDK.
**Produces:** root `:app` and `:audio:decent-usb-audio-driver` build using upstream versions, with attributable source provenance.

- [x] Verify source commit and clean reference checkout; download Gradle using checksum verification if the Java wrapper cannot follow the distribution redirect.
- [x] Preserve the old foundation through checked workspace-contained moves, and copy only tracked upstream application/build inputs. Keep existing Onda documents and CI ownership.
- [ ] Run `help`, then `testDebugUnitTest :app:assembleDebug :app:lintDebug`, saving complete logs under ignored `.tools/`.
- [ ] Resolve failures from specific compiler/toolchain evidence; record any upstream/version adjustments and external blockers.

## Task 2: Onda identity and update isolation

**Files:** `app/build.gradle.kts`, manifest, localized app-name resources, launcher assets, update manager, associated tests, root CI and portable tooling.

**Consumes:** imported upstream app.
**Produces:** independently installable Onda with upstream attribution and no LastWave APK update requests.

- [x] Write a JVM regression test asserting `checkForUpdate` produces no upstream request and never advertises another app's update; observe failure before changing behavior.
- [x] Set Onda identity/version, explicit component class resolution, app labels, launcher identity, backup exclusions, and disabled update channel. Retain native package names.
- [ ] Verify debug assemble, unit tests, manifest processing, and lint. Align CI with the verified toolchain.

## Task 3: Social models, backend contract, and policy migrations

**Files:** `app/src/main/java/com/lastwave/app/social/`, `app/src/test/java/com/lastwave/app/social/`, `supabase/migrations/`, `supabase/tests/`, `supabase/functions/recommendations/`, configuration example.

**Consumes:** provider track IDs and Supabase authenticated identity.
**Produces:** typed profiles/follows/settings/activity/track shares, authenticated recommendation response, and SQL authorization functions.

- [ ] Test default-off influence, eligibility/revocation, attachment validation, normalized blend, and deterministic exclusion/dedup behavior before implementing.
- [x] Add stable recommendation response fields: canonical track/provider identity, metadata, explanation/contributor provenance, policy/config version, (V1 returns a bounded list).
- [x] Add profiles/privacy/follows/blocks/influence/presence migrations with RLS and narrow transactional RPCs. Source taste sharing and receiver selection remain independent.
- [x] Provision Mumbai/free project, deploy migrations/function and run real authenticated-role policy checks plus live auth/refresh/logout/recommendation checks; delete temporary fixtures.

## Task 4: Social screen and existing-player integration

**Files:** social repository, ViewModel, screens, `ui/shell/MainShell.kt`, `ui/navigation/`, track sharing entry point.

**Consumes:** authenticated backend repositories and `MusicPlayer` singleton.
**Produces:** email account entry/profile setup, people search/follow, default-off taste influence, permitted friend listening, and explicit configuration/loading/error/empty states.

- [ ] Test repository failures, cancellation, sender/source identity, and unavailable attachments before adding behavior.
- [x] Add lifecycle-aware screens consistent with the upstream Compose system and protected keyboard/navigation insets.
- [x] Convert only canonical provider metadata into `PlayableTrack`; all playback uses the existing singleton.
- [ ] Add direct conversations/outbox and playable music attachments only when their backend contract and authorization checks can be verified.

## Task 5: Verification and accurate handoff

**Files:** architecture/readme/roadmap/build report, source provenance and migration status.

- [ ] Run relevant complete unit suites, debug build and lint; inspect produced APK/merged manifest.
- [x] Review changes independently with the requesting-code-review skill; fix material findings and verify again where justified.
- [ ] Document actual implemented scope and unperformed device/live-backend checks. Do not conflate source import, client UI, and verified cross-device social functionality.

## Execution evidence

The source import, social backend and account/player integration are implemented. Onda project akrsrxzfwbgjhpfmpoyc is healthy; both migrations and recommendations function V1 are deployed. Live auth and recommendation checks and transactional policy checks passed. A simultaneous influence-enable/block race passed without resurrecting influence after unblock/refollow. Test fixtures were deleted.

Seven pure ranking tests passed. Regression tests first reproduced upstream-APK opening and stale activity/live-presence issues; fixes were reviewed independently. The final Android build succeeded and passed 252 tests with zero failures/errors/skips; evidence is in .tools/onda-final-build.log. Seven ranking tests and live backend checks passed. Full-project lint was stopped after prolonged active analysis and remains incomplete.

The messaging/outbox step is explicitly deferred to the next product increment. V1 source taste uses validated provider metadata, consented listening/feedback and bounded ranking; full discovery/ML and richer social views remain later work. No Android device or emulator is attached.
