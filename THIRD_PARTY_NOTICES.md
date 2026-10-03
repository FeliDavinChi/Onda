# Third-party notices

Original Onda code is distributed under GNU GPL version 3. See [LICENSE](LICENSE).
The complete corresponding Onda source for each published APK is the Git tag linked from that release, including build scripts and dependency coordinates. Preserve this license and these notices when distributing modified builds. This software is provided without warranty as described in the license.

## Music extraction

Onda integrates [NewPipeExtractor v0.26.5](https://github.com/TeamNewPipe/NewPipeExtractor/tree/v0.26.5), copyright its contributors, under [GPL-3.0-or-later](https://github.com/TeamNewPipe/NewPipeExtractor/blob/v0.26.5/LICENSE). Provider extraction code belongs to NewPipeExtractor; Onda's adapter is independently written. The dependency's source is available at the exact tag above and its transitive dependency declarations are in its Gradle version catalog.

The extractor also brings these unmodified libraries. Their source and license texts are available at the pinned upstream versions:

- Rhino 1.8.1: [source](https://github.com/mozilla/rhino/tree/Rhino1_8_1_Release), [MPL-2.0 and embedded notices](third_party/licenses/Rhino-LICENSE.txt).
- jsoup 1.22.2: [source](https://github.com/jhy/jsoup/tree/jsoup-1.22.2), [MIT license and copyright](third_party/licenses/Jsoup-LICENSE.txt).
- nanojson, TeamNewPipe revision e9d656ddb49a412a5a0a5d5ef20ca7ef09549996: [source and copyright headers](https://github.com/TeamNewPipe/nanojson/tree/e9d656ddb49a412a5a0a5d5ef20ca7ef09549996), Apache-2.0, copyright The nanojson Authors.
- protobuf-javalite 4.35.1: [source](https://github.com/protocolbuffers/protobuf/tree/v35.1), [BSD license and copyright](third_party/licenses/Protobuf-LICENSE.txt).

These source links and license copies are bundled in the APK's `assets/licenses/` directory. [Apache-2.0](third_party/licenses/Apache-2.0.txt) is also included for the platform/playback libraries below.

## LastWave UI adaptation

Home's statistics, collection selector and pinned playback hierarchy, and the player's primary transport / secondary modes hierarchy, are adapted from [LastWave Native](https://github.com/Clash-Projects/LastWave-native/tree/ac19d0634e2d9d0f9ea47d4720b46ce53722f757), by Duxtami, Ajisth and contributors, at revision `ac19d0634e2d9d0f9ea47d4720b46ce53722f757`. The reference is distributed under [GNU GPL version 3](third_party/licenses/LastWave-GPL-3.0.txt). Onda rewrites these structures for its own music contracts, local history, navigation and selective glass. LastWave branding, album artwork and audio are not bundled.

## Playback and platform libraries

- Haze 1.5.4: [Chris Banes and contributors](https://github.com/chrisbanes/haze/tree/1.5.4), Apache License 2.0. Used for captured-background blur; no Haze sample app layouts are incorporated.
- Manrope: copyright 2018 The Manrope Project Authors (Mikhail Sharanda, Mirko Velimirovic, Alexey Kudryavtsev), [SIL Open Font License 1.1](third_party/licenses/Manrope-OFL.txt). The unmodified variable font is bundled from [Google Fonts](https://github.com/google/fonts/tree/main/ofl/manrope).

- AndroidX Media3, Compose, Room, DataStore and other AndroidX libraries: [Android Open Source Project](https://android.googlesource.com/platform/frameworks/support/), Apache License 2.0.
- Kotlin and kotlinx.coroutines / kotlinx.serialization: [JetBrains Kotlin](https://github.com/JetBrains/kotlin), Apache License 2.0.
- Hilt / Dagger: [Google Dagger](https://github.com/google/dagger), Apache License 2.0.
- OkHttp: [Square OkHttp](https://github.com/square/okhttp), Apache License 2.0.
- Coil: [Coil contributors](https://github.com/coil-kt/coil), Apache License 2.0.

All dependencies retain their own copyrights, licenses and notices. Resolved versions are pinned in `gradle/libs.versions.toml` and dependency build files; bundled upstream notices are not replaced by this summary. The music provider's availability and content permissions are separate from Onda's software license.
