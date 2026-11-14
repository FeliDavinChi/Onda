# Onda foundation

The archived first phase of Onda, an independent Android prototype built around a local demo music catalogue. For the current music player and Circle preview, see the [main Onda README](../README.md).

## What the prototype includes

- A Compose shell and a replaceable music source backed by a deterministic demo catalogue.
- Shared music and social models, a Room cache for public catalogue metadata, and saved display preferences.
- An optional Supabase client factory. Authentication and environment configuration were deferred at this stage.

See the [foundation architecture](ARCHITECTURE.md) and [verification report](BUILD_REPORT.md) for the implementation and the checks performed at the time.

## Build the archive

Use JDK 17, Android SDK 35 and build tools 35.0.0. Set `JAVA_HOME` and `ANDROID_HOME`, or put `sdk.dir` in an ignored `local.properties` inside this directory. The included wrapper pins Gradle 8.11.1.

From the repository root on Windows:

```powershell
Set-Location foundation
.\gradlew.bat test :app:assembleDebug :app:lintDebug
```

## Project background

The original [product brief](../docs/PRODUCT_BRIEF.md) and [reconnaissance](../RECONNAISSANCE.md) describe the requirements and references for this prototype. See the [source provenance](../UPSTREAM.md) for the later LastWave import used by the active app.
