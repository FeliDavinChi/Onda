<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" width="88" height="88" alt="Onda logo">
</p>

<h1 align="center">Onda</h1>

<p align="center">A native Android music player with shared listening and discovery through Onda Circle.</p>

<p align="center">
  <strong><a href="https://github.com/FeliDavinChi/Onda/releases/download/circle-v0.2.1/Onda-Circle-0.2.1.apk">Download Onda Circle 0.2.1</a></strong>
  &nbsp;·&nbsp;
  <a href="https://github.com/FeliDavinChi/Onda/releases/tag/circle-v0.2.1">Release notes and SHA-256</a>
</p>

## Onda Circle

- **Follow friends.** See listening activity that people you follow have chosen to share.
- **Blend tastes.** Choose whose shared taste should inform your recommendations. Following someone leaves this choice off.
- **Control sharing.** Use Account to manage personalization, listening activity, taste sharing, private sessions and blocked accounts. New profiles start with personalization, listening sharing and taste sharing on, and private session off.

The **0.2.1 preview** requires **Android 10 or later** and includes the Onda Supabase integration. See the [build report](BUILD_REPORT.md) for completed checks and remaining device validation.

## Build from source

| Tool | Version |
| --- | --- |
| JDK | 17 |
| Gradle wrapper | 9.3.1 |
| Android SDK platforms | 37.0 and 36 |
| Android build tools | 36.0.0 |
| NDK | 28.2.13676358 |
| CMake | 3.22.1 |
| Node.js, for ranking tests | 24 |

Set `JAVA_HOME` and `ANDROID_HOME` for an installed toolchain, or set `sdk.dir` in ignored `local.properties`. Run these commands from the repository root on Windows.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain
node --experimental-strip-types --test supabase/functions/recommendations/ranking.test.ts
```

For the portable Windows toolchain, see the [tool bootstrap script](scripts/bootstrap-tools.ps1) and [additional SDK packages script](scripts/bootstrap-fork-tools.ps1). Review the Android SDK terms before passing `-AcceptAndroidLicense`. Once the portable tools are installed, use `.\scripts\gradle-local.ps1` in place of `.\gradlew.bat`.

### Connect Circle to Supabase

Copy [`.env.example`](.env.example) to ignored `.env` and set `ONDA_SUPABASE_URL` and `ONDA_SUPABASE_PUBLISHABLE_KEY`. Android accepts a public `sb_publishable_` key. Builds without Circle configuration still support music browsing and playback.

The first recommendation ranker uses consented listening and feedback, with bounded influence from selected people and attribution for why a track appeared. See the [backend guide](supabase/README.md) for deployment and verification.

## Repository guide

| Path | Purpose |
| --- | --- |
| [`app/`](app/) and [`audio/`](audio/) | Active Android app, playback and native audio |
| [`supabase/`](supabase/) | Circle database, policies and recommendations Edge Function |
| [`foundation/`](foundation/) | Archived independent Onda prototype |

[Architecture](ARCHITECTURE.md) · [Build report](BUILD_REPORT.md) · [Roadmap](ROADMAP.md)

## Credits and license

Onda is an independent derivative of [LastWave-Native by Clash-Projects](https://github.com/Clash-Projects/LastWave-Native), imported at [`3156a434`](https://github.com/Clash-Projects/LastWave-Native/commit/3156a434d7e798a02df7e5fd47ae13f10a9c983a). Its player, catalogue, library, interface and native audio engine build on LastWave's work. Spotify's friend activity experience informed the design of Circle.

Onda preserves upstream notices and is distributed under the [GNU GPL v3](LICENSE). See [source provenance](UPSTREAM.md) for the original source and Onda's modifications.
