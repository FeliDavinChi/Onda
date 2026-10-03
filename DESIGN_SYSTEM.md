# Design system

Product name: **Onda**. Use the shared app-name resource for visible application branding.

Status: 0.4.0 UI implementation with the owner's approved LastWave-aligned Home/player hierarchy; physical-device acceptance remains a gate. Dark mode leads with deep ink surfaces, cool mint fallback accents and readable cover-derived colors. Light mode uses soft white, dark ink and visible boundaries. Static wave contours and the original three-flowing-wave mark give Onda its signature. The source-backed portable record is `DESIGN.md`; native material, motion and breakpoint extensions live in `.impeccable/design.json`. LastWave's pinned GPL-3.0 adaptation is attributed in `THIRD_PARTY_NOTICES.md`.

## Hierarchy

Content remains readable and mostly solid. Interactive surfaces have moderate tint. Floating navigation/mini-player/composer use stronger depth. Temporary sheets/menus use the strongest treatment. Dense lyrics, long messages, forms/settings and accessibility-critical text stay opaque enough for contrast.

## Tokens

| Token | Initial value / rule |
| --- | --- |
| Dark background / surface / text | #101419 / #1B222B / #F1F4F6 |
| Light background / surface / text | #F5F7F8 / #FFFFFF / #151B22 |
| Accent dark / light | #A8E6CF / #176448 |
| Spacing | 4, 8, 12, 16, 24, 32 dp |
| Shapes | 12 dp small artwork, 14 dp pinned artwork, 16 dp row, 18/24 dp statistics, 20 dp settings/skip, 22 dp play-pause, 24 dp mini-player/search action, 28 dp floating, 40 dp transport |
| Typography | Bundled Manrope (SIL OFL 1.1), Android glyph fallback, scalable sp; recent-song display 34/40, route/player/artist-stat headline 30/38, Messages headline 26/34, section title 21/28, pinned title 16/23, row title 14/20, body 14/21, destination label 11/16; full used ramp in DESIGN.md |
| Interactive minimum | 48 x 48 dp |
| Motion | Implemented cover-color transition 220 ms Full, 0 ms Reduced; Minimal skips extraction/animation. Native Material interactions remain; custom navigation/player transition timings are not implemented |
| Focus / selected | Shape and text/state cue as well as accent; never color alone |

GlassLevel: None/Subtle/Standard/Elevated/Overlay. Central Full tint alpha values: 1.0/.90/.78/.72/.88; directional border alpha 0/.08/.14/.22/.24; elevation 0/0/2/8/10 dp; blur 0/12/18/24/24 dp. One-pixel-density-independent edge light follows a diagonal gradient. None always stays opaque, including in Reduced mode.

FULL/REDUCED/MINIMAL are persisted user effect preferences. Haze 1.5.4 captures the actual quiet wave/artwork backdrop and blurs it behind floating controls; foreground controls stay sharp. Each window has one source and sibling glass surfaces. Content/other glass surfaces are never captured into that source. This is Liquid Glass-inspired blur, tint and edge light, without optical refraction. API33+ non-low-RAM devices use blur. API26–32 and low-RAM devices use opaque fallback. MINIMAL disables capture, blur, decoration and elevation; REDUCED uses 8dp blur, .94 tint and no elevation. Physical frame/battery measurements remain required before performance acceptance.

## Components and ownership

Phase 1 implements theme, GlassSurface and reusable navigation/empty/error primitives as required by its shell. GlassCard/GlassTopBar/GlassMiniPlayer/buttons/search/dialog/sheet/context menu/composer/chip/player controls are built when their feature exists, all on the same tokens. Do not create a file full of empty wrappers in advance.

Home uses a compact Onda header and 48dp glass search action, real Recent listening song/known-artist statistics in solid asymmetric Bento tiles, then a Your mix Recent/Recommended selector, existing player pinned first and plain collection rows. Statistics describe at most 20 recent unique songs on this device; they are not all-time plays. The pinned row uses 64dp artwork and a 48dp play/pause action; four ordinary rows expand through Show all. Unequal discovery Bento and truthful cardless friend activity follow. Home has no featured hero or horizontal history-artwork rail. The owner's requested statistics/selector/pinned-list and primary-transport/lower-modes hierarchy is adapted from LastWave revision ac19d0634e2d9d0f9ea47d4720b46ce53722f757 with attribution; Onda keeps its identity and native behavior.

Messages remains a truthful cardless empty layout. Search has a glass field with a persistent semantic label and actionable empty/error states. The dock keeps all five visible destination labels; at 150%+ text and below 640dp it uses three columns, or two below 340dp. Windows shorter than 500dp with 150%+ text use a horizontally scrollable row of 140dp destinations and bring the active destination into view. A floating rail appears only at width >=840dp and height >=600dp. IME hides navigation while input is active, reserving keyboard space for results. Home content is bounded at 760dp; mini-player/dock at 640dp; player column at 460dp. FullPlayer uses large artwork, cover-colored static atmosphere, metadata, cardless elapsed/remaining seeking and an independent glass island containing previous (56dp, 20dp corners), play/pause (72dp, 22dp corners) and next (56dp, 20dp corners). The cardless lower row holds shuffle, a real queue count and repeat. Every PlayPauseButton has 22dp corners, including 48dp mini-player and pinned Home controls. MiniPlayer persists outside routes. Artwork-derived colors use a cancellable software 64px request, off-main analysis and a 24-entry color cache, with 4.5:1 accent contrast checks against background/surface/variant/derived container and readable foreground selection during transitions. Minimal skips extraction and animation. Shared-element player transitions and social sharing remain future work. No nested live blur stacks.

## Accessibility and performance

TalkBack names actions with track context; selected tabs announce selected state. Decorative artwork has no repeated spoken title. All icon controls have labels; progress announces position/duration without continuously interrupting reading. Full-screen scroll and wrapping support 200% text; no fixed metadata heights. Reduce motion/effects on request. Verify text contrast at least 4.5:1 for normal text and 3:1 for large text; tint values alone do not prove that result.

Use lazy lists, stable item IDs, bounded artwork requests and lifecycle-aware Flow collection. Cache cover colors, avoid extraction during recomposition and avoid continuous blur of moving content. Test scrolling/player frame timing on representative devices and simplify effects on regressions. Deliberate actions wrapped by `tactileAction` request system-respecting native haptics; Minimal disables them. No haptics occur during continuous scroll/seeking; not every action uses this wrapper.

## Verification gates

Twenty native Robolectric/Roborazzi production-component fixtures in `.impeccable/review/` cover dark/light, large-text, narrow/short, tablet, IME, material/fallback and truthful UI states with offline fixture artwork. They include the final LastWave-aligned Home/player hierarchy; the contact sheet is `build/identity-work/evidence.jpg`. Check TalkBack order, 48 dp targets, system bar/inset behavior, visual contrast, audio/haptics and reduced effects on a real device/emulator; frame/battery acceptance remains pending. Future custom player transitions require interruption/recreation checks and macrobenchmark measurements. Source-level Compose code, host rendering and successful assemble do not replace physical checks.
