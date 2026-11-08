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

## Install

The **0.2.1 preview** requires **Android 10 or later** and includes the Onda Supabase integration. See the [build report](BUILD_REPORT.md) for completed checks and remaining device validation.

### Verify the download

Run `Get-FileHash .\Onda-Circle-0.2.1.apk -Algorithm SHA256` in PowerShell, or `sha256sum Onda-Circle-0.2.1.apk` on Linux. Compare the result with the [published SHA-256 file](https://github.com/FeliDavinChi/Onda/releases/download/circle-v0.2.1/Onda-Circle-0.2.1.apk.sha256).

## Onda Circle

- **Follow friends.** See listening activity that people you follow have chosen to share.
- **Blend tastes.** Choose whose shared taste should inform your recommendations. Following someone leaves this choice off.
- **Control sharing.** Use Account to manage personalization, listening activity, taste sharing, private sessions and blocked accounts. New profiles start with personalization, listening sharing and taste sharing on, and private session off.

### Get started in Circle

1. Open **Circle**, then sign in or choose **Create account**. Circle uses its own email account.
2. Set a display name and a username. Usernames use 3–24 letters, numbers or underscores.
3. Search in **People**, open a profile and tap **Follow**. Shared listening appears in **Friends**.

To let a friend influence your **For you** recommendations, turn on **Influence my recommendations** on their profile. The switch requires your personalization setting, an active follow and permission to use the other person's shared taste.

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

Set `JAVA_HOME` and `ANDROID_HOME` for an installed toolchain, or set `sdk.dir` in ignored `local.properties`. Run the Gradle wrapper from the repository root.

**Windows PowerShell**

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain
```

**Linux or macOS**

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain
```

With Node.js 24, run the ranking tests on either platform.

```sh
node --experimental-strip-types --test supabase/functions/recommendations/ranking.test.ts
```

For the portable Windows toolchain, see the [tool bootstrap script](scripts/bootstrap-tools.ps1) and [additional SDK packages script](scripts/bootstrap-fork-tools.ps1). Review the Android SDK terms before passing `-AcceptAndroidLicense`. Once the portable tools are installed, use `.\scripts\gradle-local.ps1` in place of `.\gradlew.bat`.

If Gradle reports an access error while replacing generated resources under OneDrive, use a checkout outside the synced folder. This also gives Gradle and CMake local output paths for intermediates and native libraries.

### Build outputs

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The [Android workflow](.github/workflows/android.yml) runs the tests, debug build and lint, then uploads the APK and verification reports as workflow artifacts.

### Connect Circle to Supabase

Copy [`.env.example`](.env.example) to ignored `.env` and set `ONDA_SUPABASE_URL` and `ONDA_SUPABASE_PUBLISHABLE_KEY`. Use the public `sb_publishable_` key. Environment variables take precedence over Gradle project properties, which take precedence over local file settings. In local files, `.env` values override matching `local.properties` entries. Builds without Circle configuration still support music browsing and playback.

The first recommendation ranker uses consented listening and feedback, with bounded influence from selected people and attribution for why a track appeared. See the [backend guide](supabase/README.md) for deployment and verification.

## Repository guide

| Path | Purpose |
| --- | --- |
| [`app/`](app/) and [`audio/`](audio/) | Active Android app, playback and native audio |
| [`supabase/`](supabase/) | Circle database, policies and recommendations Edge Function |
| [`foundation/`](foundation/) | Archived independent Onda prototype |
| [`scripts/`](scripts/) | Windows build tooling and the live Supabase verification script |
| [`.github/workflows/android.yml`](.github/workflows/android.yml) | Android checks and build artifacts |

[Architecture](ARCHITECTURE.md) · [Build report](BUILD_REPORT.md) · [Roadmap](ROADMAP.md)

## Contributor guides

- Circle user behavior and device checks are covered in the [privacy guide](docs/CIRCLE_PRIVACY.md) and [two-account smoke test](docs/CIRCLE_SMOKE_TEST.md).
- For build verification and asset changes, see [Android build checks](docs/ANDROID_CHECKS.md) and [Android branding](docs/ANDROID_BRANDING.md).
- For server checks and app integration, see [Circle backend checks](docs/CIRCLE_BACKEND_CHECKS.md) and the [recommendations API](docs/RECOMMENDATION_API.md).

## Credits and license

Onda is an independent derivative of [LastWave-Native by Clash-Projects](https://github.com/Clash-Projects/LastWave-Native), imported at [`3156a434`](https://github.com/Clash-Projects/LastWave-Native/commit/3156a434d7e798a02df7e5fd47ae13f10a9c983a). Its player, catalogue, library, interface and native audio engine build on LastWave's work. Spotify's friend activity experience informed the design of Circle.

Onda preserves upstream notices and is distributed under the [GNU GPL v3](LICENSE). See [source provenance](UPSTREAM.md) for the original source and Onda's modifications.
