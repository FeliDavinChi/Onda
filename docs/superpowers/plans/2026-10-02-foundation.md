# Android Foundation Implementation Plan

> Execute inline with the executing-plans and verification-before-completion skills. The user's initial request already authorizes Phase 1 implementation and routine technical choices.

**Goal:** A buildable independent native Android repository with tested domain/config/source behavior and real infrastructure boundaries.

**Architecture:** JVM model/contracts and source modules isolate Android/provider data. Android modules own cache, backend factory, design tokens and app composition.

**Tech Stack:** Kotlin 2.1.20, AGP 8.9.2, Gradle 8.11.1, JDK 17, SDK 35/min SDK 26, Compose BOM 2025.04.01, Hilt, Room, DataStore, Supabase Kotlin/Ktor.

## Constraints

No reference code/assets copied; no service-role/secret keys; no fake remote or playback success; no changes to parent application. No advanced features before stable playback/messaging. Future plans in root documents are not implemented claims.

## Task 1: Reproducible build and domain/config behavior

- [x] Create pinned version catalog, Gradle wrapper and JVM modules in core/model, core/common, domain/music, data/music.
- [x] Write MusicIdTest for cross-provider collisions, percent encoding and invalid namespace/blank ID.
- [x] Write BackendConfigTest for optional configuration, valid HTTPS public config, partial/plaintext/secret/unknown/embedded credentials rejection.
- [x] Run scripts/gradle-local.ps1 :core:model:test :core:common:test and verify the initial contract stubs fail behavior assertions.
- [x] Implement validated MusicId and BackendConfig, then rerun the same tests.
- [x] Add provider-independent immutable models and typed MusicSource errors/contracts.

Interfaces: MusicId(provider: String, providerId: String).canonical: String; BackendConfig.from(url: String?, key: String?): BackendConfig?; MusicSource methods return shared music models.

## Task 2: Deterministic catalog

- [x] Write DemoMusicSourceTest for normalized search, empty search, cross-provider rejection, missing stream and stable related candidates.
- [x] Verify absent behavior fails, implement DemoMusicSource with three original metadata fixtures, then run :data:music:test.
- [x] Ensure getStream returns a typed unavailable error rather than a bogus playable URL.

Files: data/music/src/main/kotlin/dev/socialmusic/data/music/DemoMusicSource.kt and matching src/test file; domain/music/src/main/kotlin/dev/socialmusic/domain/music/MusicSource.kt.

## Task 3: Infrastructure and native shell

- [x] Add Android database/network/designsystem/app build files and manifest.
- [x] Implement bounded Room CachedTrackDao and exported v1 schema; no destructive migration fallback.
- [x] Add optional Supabase factory with Auth/Postgrest/Realtime, no credentials in generated BuildConfig.
- [x] Add Hilt app/module, CatalogViewModel/repository and DataStore effect preference.
- [x] Add scalable dark/light theme, centralized GlassSurface and five navigation destinations, honest catalog loading/error/empty states.
- [x] Run test :app:assembleDebug :app:lintDebug and repair compiler/lint errors.

Files: core/database owns entities/DAO/database; core/network owns BackendClientFactory; core/designsystem owns tokens/theme/glass; app owns manifest, launch, DI, data repositories, ViewModel and navigation UI. Model MusicId and Source contract are consumed without provider-specific UI types.

## Task 4: Review and evidence

- [x] Inspect tracked tree/diff and search for placeholder/credential/privacy problems.
- [x] Record exact build/test counts and unperformed device/server checks in BUILD_REPORT.md.
- [x] Update README/ROADMAP to distinguish the shipped increment from deferred work.
- [x] Commit only new repository files after verification; preserve parent edits.

Do not call Phase 1 done unless compile/tests/lint succeed. Device and server policy checks remain explicit follow-ups even if host build succeeds.
