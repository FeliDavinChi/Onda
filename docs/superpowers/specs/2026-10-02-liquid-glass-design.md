# Onda liquid glass UI pass

Scope: bounded refinement of the existing discovery, search, navigation and player flows. The product brief delegates routine engineering decisions; the user's request explicitly selects Liquid Glass. No new account, social or library backend is introduced.

## Direction

Onda means wave: a quiet, original wave field is the signature background. Artwork stays prominent, with an ink/mint palette already established by the brief. Avoid a grid of transparent containers; track lists and settings stay readable. Manrope gives titles a rounded, open rhythm; body copy uses the same family with system glyph fallback.

Palette: Ink #101419, Slate #1B222B, Mist #F1F4F6, Seafoam #A8E6CF, Forest #176448, Paper #F5F7F8. Left-align discovery and search; center artwork and transport in the player. Use 8/16/24/32 dp spacing and 12/20/28 dp shape hierarchy.

Home: Onda wordmark, useful discovery heading, a search entrance, an artwork rail from real catalog results, then a compact track list. Search: a distinct glass search field with clear, actionable loading/empty/error states. Floating dock: five labeled destinations, persistent mini-player above it. Full player: large artwork, metadata, seek/time, one coherent glass transport surface, then queue access.

## Rendering

Use pinned Haze 1.5.4 (Apache-2.0, Kotlin 2.1 / Compose 1.7 compatible) for actual captured-background blur. Each window owns one backdrop source; floating effects never capture themselves. Draw a restrained directional edge highlight over the blurred/tinted material. This is Liquid Glass-inspired blur/tint/highlight, not optical refraction.

Full uses bounded 18–24 dp blur; Reduced uses 8 dp and no shadow; Minimal uses opaque surfaces and no blur/highlight. API26–32 and low-RAM devices use opaque fallback (the pinned Haze 1.5.x renderer enables blur on API33+). Foreground controls remain sharp. Keep dense content solid and centralize all material values.

## UX and validation

Preserve all existing live search, queue, retry and playback commands. Touch targets are at least 48 dp. Labels, state descriptions, selection semantics and clear search remain accessible. Screens scroll at 200% font scale; no fixed-height text containers. System/IME insets must not hide the last result. No fabricated library/social content.

Host-render dark/light, Minimal and 200% font variants with Robolectric Compose screenshots and inspect the images. Exercise mini-player opening, search clearing, transport/queue actions and fallback. Run the complete existing test/assemble/lint command. Host rendering does not establish physical frame rate, TalkBack or audio behavior; record that limit.
