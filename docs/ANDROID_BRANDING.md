# Android brand assets

The [Android manifest](../app/src/main/AndroidManifest.xml) selects the Onda adaptive icon. The dark background and mint mark used in the splash and on in-app screens also appear in the launcher assets.

| Surface | Resource or source |
| --- | --- |
| Launchers and widget previews | [Adaptive icons](../app/src/main/res/mipmap-anydpi-v26) and density-specific PNGs |
| Splash and in-app mark | [`onda_launcher.xml`](../app/src/main/res/drawable/onda_launcher.xml) |
| Playback notification small icon | [`ic_launcher_logo.xml`](../app/src/main/res/drawable/ic_launcher_logo.xml) |
| Widget placeholder | [`widget_art_placeholder.xml`](../app/src/main/res/drawable/widget_art_placeholder.xml) |
| Android Auto attribution | [`ic_car_attribution.xml`](../app/src/main/res/drawable/ic_car_attribution.xml) |
| Discord fallback artwork | [`DiscordPresence.kt`](../app/src/main/java/com/lastwave/app/presence/DiscordPresence.kt) selects the public Onda icon when track artwork is missing or local |

When updating the mark, check the launcher under a circular mask and with Android themed icons enabled, then inspect the splash, a widget and playback notifications with and without cover artwork. Status icons require a distinct silhouette at small sizes. The repository README uses the round PNG in the 192-pixel launcher asset directory.

Build a debug APK with [Android build checks](ANDROID_CHECKS.md) to validate packaging. Use an Android Auto test setup and Discord separately to check those external surfaces.
