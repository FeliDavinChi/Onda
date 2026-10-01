# Repository reconnaissance

Inspected: 2 October 2026 (Asia/Kolkata). Local workspace: `C:/Users/laksh/OneDrive/Documents/VoiceNotes`. User directed creation of a new repository after the workspace mismatch was identified. The new repository is `social-music/`; it starts without inherited Android code.

## Current state

The parent tree contains `frontend/` (Next.js 15, React 19, TypeScript, Tailwind, Vitest), `backend/` (FastAPI, SQLite, Python tests), `desktop/` (Tauri 2, Rust, React), and `docs/` (VoiceNotes design/plan). README describes recording lectures, transcription, analysis, exports, and search. Backend routes operate on lectures; its database stores transcript and study content. UI operates on lecture objects. API access uses fetch. Recent commits establish VoiceNotes backend and desktop/frontend foundations.

No Android manifest, Gradle build, Kotlin source, Media3 player, Supabase client, music catalog adapter, or music/social database was found in the parent application. No root application license was found by the license-file scan. Its uncommitted README and environment/example documentation changes are unrelated user work and remain untouched. No Git remote was reported. The `.agents` directory contains no instruction files.

Detected build environment: Java executable points to Oracle Java 8; Gradle, javac, adb, sdkmanager, kotlinc, Android Studio at the standard path, and the SDK at the standard user path were not detected. Android compilation requires configuring a JDK 17 and SDK. Build-tool installation and SDK-license acceptance were requested separately. No Android build or device execution has occurred at reconnaissance time.

## Reusable components

There is no source component directly reusable in the native Android product. Useful engineering patterns are repository boundaries, injectable network clients, colocated tests, and staged implementation documents. VoiceNotes lecture models, transcription pipeline, SQLite tables, browser recorder, and Tauri hotkeys do not fit this product and are not migrated.

## Technical debt and risks

The principal issue is project mismatch, resolved by starting independently. Android tooling is missing. The music provider, commercial release rights, distribution license, real Supabase instance, and production app ID remain separate release decisions. An unofficial provider must remain replaceable and may change behavior. The catalog source is not a legal entitlement to stream or distribute audio. No provider library or GPL code is currently incorporated; do not infer licensing compatibility from architectural inspiration.

## Proposed architecture and plans

Use Kotlin/Compose with a modest modular foundation; [ARCHITECTURE.md](ARCHITECTURE.md) defines music source, single-service playback, social graph and messaging boundaries. [DATABASE.md](DATABASE.md) defines local storage and the server policy plan. [RECOMMENDATIONS.md](RECOMMENDATIONS.md) defines recommendation and taste-profile behavior. [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) defines the glass hierarchy. [ROADMAP.md](ROADMAP.md) sets implementation order and gates. These documents describe proposed systems unless explicitly marked implemented.

## Reference review

Reviewed public sources, not a locally compiled reference checkout:

- [LastWave repository/README](https://github.com/Clash-Projects/LastWave-Native): native Kotlin, Compose, service-based media, library/discovery scope, GPL-3.0 label.
- [Reference Gradle build](https://github.com/Clash-Projects/LastWave-Native/blob/main/app/build.gradle.kts) and [version catalog](https://github.com/Clash-Projects/LastWave-Native/blob/main/gradle/libs.versions.toml): Compose/Hilt/Room integration and isolated upstream extraction dependencies.
- [Reference manifest](https://github.com/Clash-Projects/LastWave-Native/blob/main/app/src/main/AndroidManifest.xml): declared playback service and media foreground-service permissions.
- [Playback package](https://github.com/Clash-Projects/LastWave-Native/tree/main/app/src/main/java/com/lastwave/app/playback) and [data package](https://github.com/Clash-Projects/LastWave-Native/tree/main/app/src/main/java/com/lastwave/app/data): service/player separation and distinct music, network, discovery, artwork and lyrics packages.

GitHub returned partial directory listings; full source internals, provider behavior and the reference build were not verified. Useful patterns are service-owned playback, metadata flowing across discovery/player/library, and cached artwork themes. Adopt those ideas independently. Do not inherit broad storage permissions, advanced native audio machinery, release secrets, or unrelated integrations as foundation requirements.

Primary implementation references: [Media3 background playback](https://developer.android.com/media/media3/session/background-playback), [Supabase RLS](https://supabase.com/docs/guides/database/postgres/row-level-security), [Supabase Kotlin installation](https://supabase.com/docs/reference/kotlin/installing), [AGP 8.9 compatibility](https://developer.android.com/build/releases/agp-8-9-0-release-notes). The last source confirms Gradle 8.11.1, JDK 17 and SDK 35 compatibility. Inspection supports architecture choices; it does not establish a completed feature or release readiness.
