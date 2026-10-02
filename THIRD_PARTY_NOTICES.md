# Third-party notices

Original Onda code is distributed under GNU GPL version 3. See [LICENSE](LICENSE).
The complete corresponding Onda source for each published APK is the Git tag linked from that release, including build scripts and dependency coordinates. Preserve this license and these notices when distributing modified builds. This software is provided without warranty as described in the license.

## Music extraction

Onda integrates [NewPipeExtractor v0.26.5](https://github.com/TeamNewPipe/NewPipeExtractor/tree/v0.26.5), copyright its contributors, under [GPL-3.0-or-later](https://github.com/TeamNewPipe/NewPipeExtractor/blob/v0.26.5/LICENSE). Provider extraction code belongs to NewPipeExtractor; Onda's adapter is independently written. The dependency's source is available at the exact tag above and its transitive dependency declarations are in its Gradle version catalog.

Onda does not incorporate LastWave application source, branding, illustrations or layouts.

## Playback and platform libraries

- AndroidX Media3, Compose, Room, DataStore and other AndroidX libraries: [Android Open Source Project](https://android.googlesource.com/platform/frameworks/support/), Apache License 2.0.
- Kotlin and kotlinx.coroutines / kotlinx.serialization: [JetBrains Kotlin](https://github.com/JetBrains/kotlin), Apache License 2.0.
- Hilt / Dagger: [Google Dagger](https://github.com/google/dagger), Apache License 2.0.
- OkHttp: [Square OkHttp](https://github.com/square/okhttp), Apache License 2.0.
- Coil: [Coil contributors](https://github.com/coil-kt/coil), Apache License 2.0.

All dependencies retain their own copyrights, licenses and notices. Resolved versions are pinned in `gradle/libs.versions.toml` and dependency build files; bundled upstream notices are not replaced by this summary. The music provider's availability and content permissions are separate from Onda's software license.
