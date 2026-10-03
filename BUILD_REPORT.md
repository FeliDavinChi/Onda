# Onda build and backend report — 4 October 2026

## Onda Circle 0.2.1 update

Profile setup now accepts mixed-case usernames, normalizes them to lowercase, trims pasted input and explains invalid characters/length. New profiles default to personalization, listening sharing and taste sharing on, with private session off. The deployed `social_defaults` migration changes insertion defaults only; existing settings matched their pre-migration fingerprint. Transactional default/preservation and social-policy checks passed without retaining fixture accounts. Per-person influence still requires explicit selection.

Lyrics sliders keep a local draft and persist once on release, with presets/reset applying immediately. The modern lyrics renderer and list state are keyed together to isolate size-dependent layout generations. Exact `lyrics-ui` 1.0.19 source inspection identified retained list state/styles and an index-based cache shared across internal crossfades. The user's terminating crash was not reproduced on a device; this update addresses that lifecycle hazard and the per-drag persistence path.

The queue supports downward dragging from its header/body and an unconsumed downward pull at the top of the song list. Ordinary scrolling, reversal and cancellation remain separate from dismissal.

Regression baselines failed before the fixes: profile input 3 of 5, lyrics adjustment 3 of 4, queue pull-down 4 of 5. The final `:app:testDebugUnitTest :app:assembleDebug --offline` run passed in 3 minutes 49 seconds with **266 tests, zero failures/errors/skips**. The 7 ranking tests also passed. A read-only review found no critical or important defects. Evidence is in ignored `.tools/onda-0.2.1-build.log` and JUnit reports.

The fresh debug APK is 122,789,630 bytes, install ID `dev.socialmusic.app`, version `0.2.1` / code `2`, with the configured Onda public Supabase integration. SHA-256: `d743ff5501bc2e3cc8a28e5ad8f86d1330c1d49bcb27f157e13f62e8827fe94a`. Its signing certificate matches the previous Circle preview, so it can update that installation. No device is attached; touch gestures and the reported native lyrics crash still require device confirmation. Full-project lint remains incomplete.

The sections below record the initial 0.2.0 Circle preview verification.

## Source and implemented increment

The active application derives from LastWave main commit 3156a434d7e798a02df7e5fd47ae13f10a9c983a. The earlier independent Onda foundation is preserved under foundation/ and excluded from the active build. Source license/provenance are in LICENSE and UPSTREAM.md.

Onda adds its own install identity and branding, isolated Last.fm callback, disabled upstream APK updater, independent release-signing configuration, backup exclusions, and a Circle tab. Circle includes email accounts/profile setup, following, shared listening, explicit per-person taste influence, hiding activity, blocking/unblocking, private sessions and consent controls. All playback resolves validated provider metadata through the existing player. Keystore encryption protects saved account tokens; account changes cancel old requests/results.

## Supabase evidence

Created Onda project akrsrxzfwbgjhpfmpoyc in FeliDavinChi's Org, Mumbai/ap-south-1, on the confirmed $0/month free plan. The project is active/healthy. Both tracked migrations and recommendations Edge Function V1 were deployed through the connected Supabase account. The ignored Android .env contains its public URL/publishable key.

Executed tests/social_policy.sql against the real database under authenticated roles, with fixture accounts inside a rolled-back transaction. Checks passed for private defaults, raw-event isolation, rejection of arbitrary client writes, followers-only presence, follow-versus-influence separation, permission-scoped shared taste/metadata, independent activity hiding, private-session collection suppression, consent revocation, unfollow/refollow, block/unblock and own blocked controls.

Two concurrent database sessions exercised an influence enable against blocking. Unblocking/refollowing did not restore the raced selection. A live temporary email/password account verified sign-in, private defaults, event-backed personal recommendations through the deployed function, token refresh and logout. Missing/invalid authentication was rejected. Fixture accounts were removed; auth.users count returned zero after cleanup.

Security advisors reported the intentional authenticated SECURITY DEFINER RPC category only: fixed search_path, caller-derived ownership and narrow internal checks are required for these APIs. Performance advisors returned no findings after the second migration. See supabase/README.md for the remediation reference and operational details.

## Local build evidence

Portable JDK 17, Gradle 9.3.1, SDK 37.0/36, build-tools 36.0.0, NDK 28.2.13676358 and CMake 3.22.1 were installed from official distributions with checksum validation. Native audio built for arm64-v8a, armeabi-v7a, x86 and x86_64.

Seven ranking tests passed using Node 24. The upstream APK-opening regression was reproduced before disabling the updater. Privacy tests reproduced stale activity after a failed refresh and lingering live presence after clearing playback, then fixes were applied and independently reviewed. The first full Android suite passed 249 tests with zero failures/errors and assembled the debug APK.

The final :app:testDebugUnitTest :app:assembleDebug --offline run succeeded in 5 minutes 16 seconds: 252 tests, zero failures/errors/skips, and a fresh debug APK. Evidence is in ignored .tools/onda-final-build.log and app/build/test-results/testDebugUnitTest/. The preceding full run is in .tools/onda-verification.log; its full-project lint analysis remained active for an unusually long time and was stopped to rebuild the final source. No completed full lint result is claimed.

APK: app/build/outputs/apk/debug/app-debug.apk, application ID dev.socialmusic.app, version 0.2.0 (code 1), label Onda, min SDK 29 and target SDK 35, four native ABIs. Built public Supabase configuration is present. Size: 122309374 bytes. SHA-256: e607217a41fb0173a0e0795afe9b72e9a9d18401b888e0744569ebc91d701df5. No device installation or playback success is inferred from the build.

## Remaining validation and product work

No emulator or physical Android device is attached. Installation, screen layout, accessibility, actual stream playback, notifications, background service behavior and two-device friend activity still require device testing. The live API tests verify server behavior, not Android Keystore or UI execution on a device.

The recommendation engine currently ranks bounded candidates from consented listening/feedback. It has a portable versioned API and bounded social influence; advanced catalogue discovery, diversity, model training and Python hosting remain later increments. Messaging/outbox, richer follower management, retention automation, recovery/deletion flows, configured SMTP, Realtime and release delivery remain work before a broader beta/production release. No release APK signing credentials were supplied.
