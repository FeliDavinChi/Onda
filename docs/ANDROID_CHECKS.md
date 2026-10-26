# Android build checks

Run these commands from Onda's root with the [pinned toolchain](../README.md#build-from-source). On Windows, use `gradlew.bat` instead of `./gradlew`.

| Check | Command | Where to look |
| --- | --- | --- |
| JVM tests | `./gradlew :app:testDebugUnitTest` | `app/build/reports/tests/testDebugUnitTest/` |
| Debug packaging | `./gradlew :app:assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Static Android checks | `./gradlew :app:lintDebug` | `app/build/reports/lint-results-debug.html` |
| Ranking | `node --experimental-strip-types --test supabase/functions/recommendations/ranking.test.ts` | Node's test output |

When a combined build fails, rerun its first failing task on its own with `--console=plain`. Missing Android packages are configured by the [CI workflow](../.github/workflows/android.yml). Test and lint output are published in the workflow's `verification-reports` artifact, and the assembled APK in `onda-debug-apk`.

For device verification, use a test Android device with API 29 or newer, run `adb devices`, and install the debug APK with `adb install -r app/build/outputs/apk/debug/app-debug.apk`. An existing installation requires the same signing key for an in-place update. Record your results separately from the JVM build checks.
