# Onda editorial frontend

The owner's 3 October direction is the design authority: dark content-first, editorial iOS-inspired hierarchy on native Android, selective glass islands, immersive artwork, extracted music colors and clean social layouts. Routine choices remain delegated by the original brief. Apply Impeccable in Operate mode to the existing visual world; preserve Manrope/ink/mint, native Back/touch/inset behavior and all music commands.

## Surface contract

The owner subsequently approved a closer LastWave structure before publication. Home shows compact Onda branding/search, real recent-song/artist statistics when history exists, a Recent/Recommended collection selector and a pinned existing player before plain music rows. Counts describe the bounded local history, not all-time plays. Recommendations use actual music data; a restrained discovery Bento pairs unequal artwork tiles, then readable rows. Recently played is backed by bounded private local metadata. Friend activity is a quiet cardless empty section with accurate availability copy; Messages is a conversation-oriented empty layout, with no fake people, unread counts or send controls.

Now Playing owns the visual emphasis: large square cover, strong title/artist, progress with remaining time, an independent three-button floating transport island and a secondary shuffle/queue/repeat row. A bounded dominant-color extraction from the current cover colors its atmosphere and primary controls; foreground contrast is chosen mathematically. No continuously animated backgrounds. Glass is reserved for dock, mini-player, player controls and appropriate overlays. Dense rows/settings/social sections remain solid or cardless. Add an original wave mark and matching launcher identity.

## Behavior and data

Keep one existing service-owned player and a single Coil image loader. Derive colors from a software 64px request, analyze off the UI thread and cache at most 24 cover colors. Missing/artwork decode errors fall back to brand colors; cancellation/stale completions cannot color a later track. Minimal skips artwork-color work and animation.

Record only actual PLAYING tracks into a private DataStore list of at most 20 unique identities, newest first. Strip arbitrary metadata, bound retained fields and serialized bytes, tolerate malformed data, and expose clear history. A process-lifetime recorder uses the existing controller state; history write failures cannot interrupt playback. Catalog recommendations may use the newest actual listening identity as the existing provider seed; no new source or backend is introduced.

## Verification

Meaningful tests cover history ordering/bounds/corruption/actual-playing recording, color extraction and contrast, and production Home/player/social actions. Batch the existing dark/light/large-text/narrow/short/IME renders with the new UI states, inspect once, fix as a batch, confirm once. Impeccable's finish reviewer and documenter receive fresh packets. Its web detector is inapplicable to Kotlin. Native host rendering is explicitly labeled; emulator/device capture and physical accessibility/performance remain open if no device tooling is available. Full test/assemble/lint precede a new development APK/source release.
