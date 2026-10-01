# Onda

Onda is a native Android application built around music as a social object. This is a new, independent repository; the surrounding VoiceNotes project is unrelated.

## Status

Repository reconnaissance and architecture planning are documented in [RECONNAISSANCE.md](RECONNAISSANCE.md). The first Phase 1 foundation increment compiles, builds a debug APK, and passes tests and lint (with dependency-update warnings). See [BUILD_REPORT.md](BUILD_REPORT.md) for evidence and unperformed device/server checks. This repository is not a production release. Playback, authentication, social features, and real messaging are subsequent milestones, with explicit verification gates in [ROADMAP.md](ROADMAP.md).

## Build

Use JDK 17 and Android SDK 35, including build-tools 35.0.0. Set `JAVA_HOME` and `ANDROID_HOME`, or set `sdk.dir` in ignored `local.properties`.

```powershell
.\gradlew.bat test :app:assembleDebug :app:lintDebug
```

Gradle 8.11.1 and dependencies are pinned. The SDK/AGP baseline is an initial reproducible development baseline, not a claim of meeting future Play release requirements. Review target SDK and dependencies before release.

This checkout also has ignored portable tools. Run `scripts/gradle-local.ps1 test :app:assembleDebug :app:lintDebug` to use them. For a fresh Windows checkout, `scripts/bootstrap-tools.ps1` downloads verified tools; pass `-AcceptAndroidLicense` only after reviewing and accepting [Android SDK terms](https://developer.android.com/studio/terms). The helper keeps the tools and Gradle cache within `.tools/`.

## Configuration

The initial app uses an explicitly local demo catalog. The optional Supabase factory is infrastructure only: this increment has no authentication flow or environment-to-client configuration wiring. At the auth milestone, supply an HTTPS project URL and public publishable key through validated injected configuration; never add a service-role or secret key. Composables never access backend configuration. Deployment and production credentials are outside this increment.

## Engineering documents

The original user requirements are preserved in [docs/PRODUCT_BRIEF.md](docs/PRODUCT_BRIEF.md).

- [ARCHITECTURE.md](ARCHITECTURE.md): boundaries, music source, playback, social, messaging, offline behavior.
- [DATABASE.md](DATABASE.md): server schema plan, authorization, policy test matrix, local cache.
- [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md): visual hierarchy, tokens, effects, accessibility.
- [RECOMMENDATIONS.md](RECOMMENDATIONS.md): transparent hybrid ranking, taste, privacy, tests.
- [ROADMAP.md](ROADMAP.md): phased scope and acceptance gates.

## Reference and ownership

LastWave is a GPL-3.0 reference project. No LastWave code, branding, assets, or layouts are incorporated. See [references](RECONNAISSANCE.md#reference-review) for the inspected sources. This repository does not yet grant a distribution license for original application code; select one before public distribution. Third-party dependency licenses remain applicable. A future provider dependency must receive a specific license and compatibility review before incorporation.
