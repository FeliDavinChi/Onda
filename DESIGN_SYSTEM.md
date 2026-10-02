# Design system

Product name: **Onda**. Use the shared app-name resource for visible application branding.

Status: 0.3.0 UI implementation; physical-device acceptance remains a gate. Original visual identity, independent of reference branding/screens. Dark mode leads with deep ink surfaces, cool mint accents and restrained warm artwork colors. Light mode uses soft white, dark ink and visible boundaries. Static wave contours give Onda its signature backdrop.

## Hierarchy

Content remains readable and mostly solid. Interactive surfaces have moderate tint. Floating navigation/mini-player/composer use stronger depth. Temporary sheets/menus use the strongest treatment. Dense lyrics, long messages, forms/settings and accessibility-critical text stay opaque enough for contrast.

## Tokens

| Token | Initial value / rule |
| --- | --- |
| Dark background / surface / text | #101419 / #1B222B / #F1F4F6 |
| Light background / surface / text | #F5F7F8 / #FFFFFF / #151B22 |
| Accent dark / light | #A8E6CF / #176448 |
| Spacing | 4, 8, 12, 16, 24, 32 dp |
| Shapes | 12 dp small, 20 dp card, 28 dp floating |
| Typography | Bundled Manrope (SIL OFL 1.1), Android glyph fallback, scalable sp; display 34/40, headline 26/34, title 16/23, body 14/21, label 11/16 |
| Interactive minimum | 48 x 48 dp |
| Motion | 180 ms controls, 260 ms navigation; reduced mode shortens; minimal removes nonessential motion |
| Focus / selected | Shape and text/state cue as well as accent; never color alone |

GlassLevel: None/Subtle/Standard/Elevated/Overlay. Central Full tint alpha values: 1.0/.90/.78/.72/.88; directional border alpha 0/.08/.14/.22/.24; elevation 0/0/2/8/10 dp; blur 0/12/18/24/24 dp. One-pixel-density-independent edge light follows a diagonal gradient. None always stays opaque, including in Reduced mode.

FULL/REDUCED/MINIMAL are persisted user effect preferences. Haze 1.5.4 captures the actual quiet wave/artwork backdrop and blurs it behind floating controls; foreground controls stay sharp. Each window has one source and sibling glass surfaces. Content/other glass surfaces are never captured into that source. This is Liquid Glass-inspired blur, tint and edge light, without optical refraction. API33+ non-low-RAM devices use blur. API26–32 and low-RAM devices use opaque fallback. MINIMAL disables capture, blur, decoration and elevation; REDUCED uses 8dp blur, .94 tint and no elevation. Physical frame/battery measurements remain required before performance acceptance.

## Components and ownership

Phase 1 implements theme, GlassSurface and reusable navigation/empty/error primitives as required by its shell. GlassCard/GlassTopBar/GlassMiniPlayer/buttons/search/dialog/sheet/context menu/composer/chip/player controls are built when their feature exists, all on the same tokens. Do not create a file full of empty wrappers in advance.

Home uses real catalog artwork in a horizontal rail, then compact readable track rows. Search has a glass field with a persistent semantic label and actionable empty/error states. The dock keeps all five visible destination labels; at 150%+ text it becomes two rows (three on narrow windows) so names stay readable. Windows shorter than 500dp retain one row with wrapping labels, preserving a usable content viewport in split screen. IME hides the dock while input is active, reserving keyboard space for results. FullPlayer uses artwork, a bounded faint artwork ambient layer, metadata, one glass seek/transport surface and queue access. MiniPlayer persists outside routes. Artwork-derived palette extraction, shared-element player transitions and social sharing are future work; they are not implied by this UI pass. No nested live blur stacks.

## Accessibility and performance

TalkBack names actions with track context; selected tabs announce selected state. Decorative artwork has no repeated spoken title. All icon controls have labels; progress announces position/duration without continuously interrupting reading. Full-screen scroll and wrapping support 200% text; no fixed metadata heights. Reduce motion/effects on request. Verify text contrast at least 4.5:1 for normal text and 3:1 for large text; tint values alone do not prove that result.

Use lazy lists, stable item IDs, bounded artwork requests and lifecycle-aware Flow collection. Cache palettes, avoid extraction during recomposition and avoid continuous blur of moving content. Test scrolling/player transition frame timing on representative devices and simplify effects on regressions. Haptics occur for deliberate actions, never continuous scroll.

## Verification gates

Check dark and light screenshots, large font, TalkBack order, 48 dp targets, system bar/inset behavior, contrast and reduced effects on a real device/emulator. Player transitions later require interruption/recreation checks and macrobenchmark measurements. Source-level Compose code and successful assemble do not replace these checks.
