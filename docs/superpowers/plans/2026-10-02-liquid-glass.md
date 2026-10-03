# Liquid Glass UI Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline, then request a fresh review of the final changes.

**Goal:** Polish the existing native Onda music flows with readable Liquid Glass-inspired controls.

**Architecture:** One Haze source per window behind floating controls; all material/effect values belong to core/designsystem. Stateless UI content functions retain ViewModel wrappers and let host rendering verify the production components.

**Tech stack:** Kotlin 2.1.20, existing Compose BOM 2025.04.01, Haze 1.5.4, Manrope/OFL, Robolectric SDK35 Compose tests.

**Spec:** ../specs/2026-10-02-liquid-glass-design.md

## Global constraints

- minSDK26, compile/target35; preserve the provider and single service-owned player.
- No nested blur sources, optical-refraction claims, fake social data or credentials.
- 48 dp touch targets; scalable text; opaque Minimal/API26–30/low-RAM fallback.
- Keep the current worktree and public source/release workflow.

## Review focus

- 200% fonts: all controls remain reachable by scrolling.
- IME: search and the final result are reachable without dock overlap.
- Contrast: dark/light artwork never sits directly behind foreground labels.
- Playback: buffering/error labels and queue occurrence actions retain behavior.
- Fallback: no RenderEffect path on older/low-RAM devices or Minimal.

### Task 1: Shared material and typography

Files: core/designsystem/GlassSurface.kt, Theme.kt, new WaveBackdrop.kt and font resources; version catalog and notices.
- [x] Add Haze and licensed Manrope. Keep dependency versions compatible with the current compiler.
- [x] Introduce LocalGlassBackdrop / LocalEffectLevel, central blur/tint/highlight tokens, and a single-source modifier.
- [x] Replace tint-only GlassSurface with captured backdrop rendering plus readable fallback.
- [x] Compile design system before integrating screens.

### Task 2: Discovery and navigation

Files: SocialMusicApp.kt, HomeScreen.kt, ExploreScreen.kt, TrackComponents.kt, EmptyScreens.kt, strings.xml.
- [x] Extract stateless OndaScaffold, HomeContent and ExploreContent for production and host rendering.
- [x] Add floating labeled dock, real artwork discovery rail, glass search entrance/field and clear result hierarchy.
- [x] Keep real empty/error/retry state and improve settings/roadmap empty-state copy.
- [x] Verify search clearing, track/queue callbacks and mini-player opening in Compose tests.

### Task 3: Player and visual review

Files: PlayerUi.kt, app test configuration and new LiquidGlassUiTest.kt.
- [x] Give the full player its own backdrop source and coherent transport glass surface; retain seek/shuffle/repeat/error/queue commands.
- [x] Render production screens in dark/light, Full/Minimal, and 200% fonts at SDK35; inspect PNGs and repair issues.
- [x] Exercise fallback on SDK28 and preserve accessible action labels.

### Task 4: Delivery

Files: DESIGN_SYSTEM.md, BUILD_REPORT.md, README.md, release metadata.
- [x] Run test :app:assembleDebug :app:lintDebug and review changes independently.
- [x] Document actual rendering and remaining physical-device limits.
- [ ] Commit/push the UI milestone and publish a versioned APK with corresponding source in GitHub Releases.
