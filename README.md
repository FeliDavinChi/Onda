# Onda

Onda is a native Android application built around music as a social object.

## Status

Onda 0.2.0 adds live YouTube Music song search/discovery and a Media3 background playback service, mini/full player, seek, shuffle/repeat, and editable queue with paused restart restoration. Live search and a bounded audio-byte request have passed, alongside host tests, debug assembly and lint. Physical audio, background/lockscreen/headset behavior and rendered accessibility still need device verification; this is a development preview. Authentication, library persistence, social features and messaging remain future milestones. See [BUILD_REPORT.md](BUILD_REPORT.md) and [ROADMAP.md](ROADMAP.md).

Project folder: `C:/Users/laksh/OneDrive/Documents/ChatGPT/Onda`.

## Build

Use JDK 17 and Android SDK 35, including build-tools 35.0.0. Set `JAVA_HOME` and `ANDROID_HOME`, or set `sdk.dir` in ignored `local.properties`.

```powershell
.\gradlew.bat test :app:assembleDebug :app:lintDebug
```

Gradle 8.11.1 and dependencies are pinned. The SDK/AGP baseline is an initial reproducible development baseline, not a claim of meeting future Play release requirements. Review target SDK and dependencies before release.

This checkout also has ignored portable tools. Run `scripts/gradle-local.ps1 test :app:assembleDebug :app:lintDebug` to use them. For a fresh Windows checkout, `scripts/bootstrap-tools.ps1` downloads verified tools; pass `-AcceptAndroidLicense` only after reviewing and accepting [Android SDK terms](https://developer.android.com/studio/terms). The helper keeps the tools and Gradle cache within `.tools/`.

## Configuration

The app uses NewPipeExtractor v0.26.5 through an isolated adapter; no API key is required. It is an unofficial integration, so upstream changes, region restrictions or unavailable tracks can cause explicit failures. Search is limited to songs; album, artist and playlist detail remain unsupported. Stream URLs are resolved just before playback and never saved in queue snapshots. The metadata-only demo is retained as a fixture and is not the production binding.

The optional Supabase factory is infrastructure only: no authentication or app configuration wiring exists yet. At the auth milestone, supply an HTTPS project URL and public publishable key through validated injected configuration; never add a service-role or secret key.

Live provider tests are separate from ordinary CI because they depend on the upstream service:

```powershell
$env:ONDA_LIVE_PROVIDER_TEST = 'true'
.\scripts\gradle-local.ps1 :data:music:test --tests '*YoutubeLiveSmokeTest'
Remove-Item Env:ONDA_LIVE_PROVIDER_TEST
```

## Engineering documents

The original user requirements are preserved in [docs/PRODUCT_BRIEF.md](docs/PRODUCT_BRIEF.md).

- [ARCHITECTURE.md](ARCHITECTURE.md): boundaries, music source, playback, social, messaging, offline behavior.
- [DATABASE.md](DATABASE.md): server schema plan, authorization, policy test matrix, local cache.
- [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md): visual hierarchy, tokens, effects, accessibility.
- [RECOMMENDATIONS.md](RECOMMENDATIONS.md): transparent hybrid ranking, taste, privacy, tests.
- [ROADMAP.md](ROADMAP.md): phased scope and acceptance gates.

## Reference and ownership

Original Onda code is licensed under [GNU GPL version 3](LICENSE), as approved by the project owner. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for dependency licenses and source. Each APK release links its corresponding source tag; license texts/notices are also packaged in the APK. LastWave is a reference project; no LastWave code, branding, assets or layouts are incorporated.
