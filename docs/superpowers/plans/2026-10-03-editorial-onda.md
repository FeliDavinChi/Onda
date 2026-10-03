# Onda Editorial Frontend Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline; Impeccable supplies fresh finish-review/documentation handoffs.

**Goal:** Turn Onda's current music flows into the owner's editorial, artwork-driven social music design.

**Architecture:** Existing Compose/ViewModel/service boundaries remain. App-level bounded local history observes the existing player; a small artwork-color utility feeds presentation only. Production stateless content stays host-testable.

**Tech Stack:** Existing Kotlin 2.1.20, Compose BOM 2025.04.01, Coil3.1, DataStore and Haze1.5.4. No new app dependencies.

**Spec:** ../specs/2026-10-03-editorial-onda-design.md

## Global constraints

- SDK26–35, one service-owned player, 48dp controls, 200% text and system/IME insets.
- The owner approved adapting LastWave's Home/player structure and GPL-3.0 reuse; preserve Onda branding and credit the pinned upstream. No LastWave artwork/audio is bundled.
- No fake social data or unsupported messaging actions.
- Color extraction/cache and history storage are bounded; stream URLs are never retained.
- Minimal effects skip color extraction/nonessential motion; older/low-RAM fallback stays readable.

## Review focus

- A cover decode failure or late completion cannot replace the current track's accent.
- Restored paused queues are not misrepresented as listening history.
- Long track/artist names and large text retain playable/tappable layouts.
- Dark/light extracted colors keep text/icons legible.
- Social empty states remain truthful and uncluttered.

### Task 1: Real listening continuity

Files: app/data/ListeningHistory.kt and recorder; SocialMusicApplication, CatalogRepository; app/data tests.
- [x] Write/run failing history/recording tests, implement bounded private persistence and playback observation, and verify.
- [x] Seed recommendations from actual local history, preserving fallback catalog behavior; expose clear history in Profile.

### Task 2: Editorial surfaces and artwork color

Files: app/ui HomeScreen, PlayerUi, EmptyScreens, ArtworkColors, SocialMusicApp; shared OndaMark, launcher resources and strings.
- [x] Verify pinned Coil API and build cached, cancellation-safe cover-color extraction with contrast tests.
- [x] Implement compact branding, bounded listening statistics, collection selector, pinned music, asymmetric discovery and cardless social states.
- [x] Refine immersive player with three primary transports, secondary modes and remaining time, preserving commands and accessibility.

### Task 3: Review and delivery

- [x] Batch native host renders/interactions, inspect and confirm before fresh review; re-review the owner's subsequent LastWave structure change.
- [x] Complete Impeccable review/documentation, full tests/APK/lint and artifact identity checks.
- [x] Prepare the reviewed milestone for branch/tag publication and versioned APK/corresponding source delivery in Releases; remote CI and publication are verified during delivery.
