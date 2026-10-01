# Design system

Status: foundation direction; each shipped component must be verified on device. Original visual identity, independent of reference branding/screens. Dark mode leads with deep ink surfaces, cool mint accents and restrained warm artwork colors. Light mode uses warm white, dark ink and visible boundaries.

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
| Typography | Material 3 semantic roles; system sans; scalable sp |
| Interactive minimum | 48 x 48 dp |
| Motion | 180 ms controls, 260 ms navigation; reduced mode shortens; minimal removes nonessential motion |
| Focus / selected | Shape and text/state cue as well as accent; never color alone |

GlassLevel: None/Subtle/Standard/Elevated/Overlay. Central alpha values: 1.0/.97/.94/.92/.96 for readable surface tint; borders 0/.08/.12/.16/.18; elevation 0/0/2/6/10 dp. These are foundation tint/elevation values, not a claim of true backdrop refraction. Transparency composes against background; text uses foreground appropriate to the effective surface.

FULL/REDUCED/MINIMAL are persisted user effect preferences. Initial foundation supports tint/border/elevation; blur is deliberately zero until a measured backdrop implementation exists. MINIMAL uses opaque surfaces with zero decoration/elevation. REDUCED limits elevation. FULL permits richer tint/depth. Future blur radii, saturation, highlight and springs belong in the same token set; do not spread magic values across features or label a foreground blur as backdrop glass.

## Components and ownership

Phase 1 implements theme, GlassSurface and reusable navigation/empty/error primitives as required by its shell. GlassCard/GlassTopBar/GlassMiniPlayer/buttons/search/dialog/sheet/context menu/composer/chip/player controls are built when their feature exists, all on the same tokens. Do not create a file full of empty wrappers in advance.

FullPlayer uses artwork -> cached ambient palette -> metadata -> one coherent playback surface -> secondary actions. MiniPlayer floats over bottom navigation outside routes. TrackShareCard preserves track identity and connects to that player; it is not a generic link preview. No nested live blur stacks. Bright artwork uses darker supporting tint, dark artwork lighter edges, saturated artwork reduced background saturation. Palette colors are hints; accessible foreground/background pairs win.

## Accessibility and performance

TalkBack names actions with track context; selected tabs announce selected state. Decorative artwork has no repeated spoken title. All icon controls have labels; progress announces position/duration without continuously interrupting reading. Full-screen scroll and wrapping support 200% text; no fixed metadata heights. Reduce motion/effects on request. Verify text contrast at least 4.5:1 for normal text and 3:1 for large text; tint values alone do not prove that result.

Use lazy lists, stable item IDs, bounded artwork requests and lifecycle-aware Flow collection. Cache palettes, avoid extraction during recomposition and avoid continuous blur of moving content. Test scrolling/player transition frame timing on representative devices and simplify effects on regressions. Haptics occur for deliberate actions, never continuous scroll.

## Verification gates

Check dark and light screenshots, large font, TalkBack order, 48 dp targets, system bar/inset behavior, contrast and reduced effects on a real device/emulator. Player transitions later require interruption/recreation checks and macrobenchmark measurements. Source-level Compose code and successful assemble do not replace these checks.
