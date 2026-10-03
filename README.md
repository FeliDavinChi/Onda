# Onda

A native Android music app built from LastWave, with an Onda Circle for following friends, seeing shared listening and explicitly blending a person's shared music taste into recommendations.

## Fork and inspiration

Onda is a fork and modified derivative of [LastWave-Native by Clash-Projects](https://github.com/Clash-Projects/LastWave-Native), imported from commit [`3156a434`](https://github.com/Clash-Projects/LastWave-Native/commit/3156a434d7e798a02df7e5fd47ae13f10a9c983a). It substantially reuses LastWave's Android application, player, catalogue, library, interface and native audio engine. Credit for that foundation belongs to LastWave and its contributors.

Onda adds its own social backend and Circle: following, shared listening, privacy controls and an explicit choice to blend a friend's shared music taste into recommendations. Spotify's following and friend activity experience informs the social interaction design.

The active application lives in `app/` and `audio/`. The earlier independent Onda foundation is preserved in `foundation/`. See [UPSTREAM.md](UPSTREAM.md) for source provenance and modifications. LastWave's [GPL-3.0 license](LICENSE) and notices are preserved; Onda is distributed under that license. Onda is an independent project.

## Download

[Download Onda Circle 0.2.1](https://github.com/FeliDavinChi/Onda/releases/download/circle-v0.2.1/Onda-Circle-0.2.1.apk), or visit the [preview release](https://github.com/FeliDavinChi/Onda/releases/tag/circle-v0.2.1) for its SHA-256 checksum. This development build includes the configured Onda Supabase integration. Android 10 or later is required. Review [BUILD_REPORT.md](BUILD_REPORT.md) for validation and remaining limitations.

## Current integration

Onda's Supabase project is provisioned in Mumbai on the free plan. Its migrations and recommendations Edge Function are deployed. Android has email sign-in/signup, profile setup, follow/unfollow, shared listening, taste influence, hiding activity, blocking and privacy controls. New profiles start with personalized recommendations, listening sharing and taste sharing enabled, and private session off. Users can change each switch in Account; existing saved choices are preserved. Follow alone does not enable influence.

Supabase owns transactional/social data; the versioned recommendation API can later move to Python. This first ranker uses consented listening/feedback candidates and a bounded social weight, with explainable attribution. It is not yet a full learned discovery engine.

## Build

Use JDK 17, Gradle 9.3.1, SDK platforms 37.0 and 36, build-tools 36.0.0, NDK 28.2.13676358 and CMake 3.22.1. Set JAVA_HOME and ANDROID_HOME, or use ignored local.properties for sdk.dir.

```powershell
.\scripts\gradle-local.ps1 :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --console=plain
node --experimental-strip-types --test supabase/functions/recommendations/ranking.test.ts
```

For a fresh Windows checkout, scripts/bootstrap-tools.ps1 downloads verified tools. Pass -AcceptAndroidLicense only after reviewing the Android SDK terms. The local tools/cache live in ignored .tools/. A normal SDK/JDK installation can use gradlew.bat directly.

Copy .env.example to ignored .env and supply ONDA_SUPABASE_URL and ONDA_SUPABASE_PUBLISHABLE_KEY. Only a public sb_publishable_ key is accepted by Android. Never add a service-role or secret key. Unconfigured builds show account availability honestly and retain music browsing/playback.

## Verification and remaining work

See BUILD_REPORT.md for executed checks and exact limitations, ARCHITECTURE.md for boundaries, and supabase/README.md for deployment and tests. No emulator or Android device is currently attached; screen, background playback and notification testing remain necessary. Supabase email confirmation follows project configuration; public email delivery needs a configured SMTP provider before a wider beta.

This implementation increment is not a production release. Messaging, recovery/deletion flows, richer catalogue recommendations and broader social management remain in the product roadmap.
