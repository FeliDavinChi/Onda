# Onda

<!-- impeccable:product-schema 1 -->

## Platform

android

## Product Purpose

Music as a social object: listen, discover, revisit, share and discuss music. The confirmed direction is a premium social music application with an editorial hierarchy, immersive artwork, selective Liquid Glass and floating playback islands.

## Users

Music listeners who want discovery, listening continuity and eventually conversation around songs. Further audience segmentation is undecided.

## Operating Context

Native Kotlin/Compose Android app, min SDK26 and target35. Five destinations: Home, Explore, Library, Messages and Profile. The service owns the single Media3 player. The existing dark/light identity uses Manrope and ink/mint colors.

## Capabilities and Constraints

Live song discovery/search, playback, seeking, shuffle/repeat, queue edits, retry and paused restoration exist. Account authentication, friend activity, persistent saved library and messaging backends are not implemented. This design pass adds local listening continuity and artwork-derived colors without fabricating social data or importing reference application assets.

## Brand Commitments

The name is Onda. The owner explicitly requests dark content-first layouts, strong typography, large artwork, clean cardless social presentation, selective glass on floating controls, and Bento layouts limited to discovery/statistics. The owner explicitly permits LastWave reuse and requested closer screen structures for this release. Adapt its GPL-3.0 UI patterns with attribution while keeping Onda branding, selective glass and the social direction. Reference album artwork and audio are not app assets.

## Evidence on Hand

The detailed original brief is docs/PRODUCT_BRIEF.md. Production components have native Robolectric screenshot/interaction fixtures; fixture artwork is original and labeled as test data. Published APK/source previews are in the GitHub repository's Releases. No device/emulator is currently attached, and no live social data is available.

## Product Principles

- Music and content lead; decoration supports a task.
- Social interaction supports music discovery and conversation.
- Real states, accessible controls and clear recovery outrank promotional copy.
- Original visual identity and corresponding source accompany distribution.

## Accessibility & Inclusion

48dp controls, scalable text through 200%, dark/light contrast, selected/action semantics, system/IME insets, and reduced/minimal effects. Physical TalkBack and frame/battery checks remain separate device gates.
