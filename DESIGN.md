---
name: Onda
description: Premium social music with dark content-first layouts and selective Liquid Glass.
colors:
  dark-mint: "#A8E6CF"
  mint-content: "#0C3526"
  dark-primary-container: "#20483A"
  dark-container-content: "#D6F6E9"
  dark-secondary: "#B9D6E5"
  dark-secondary-content: "#173440"
  ink-background: "#101419"
  ink-surface: "#1B222B"
  ink-surface-variant: "#29323D"
  ink-text: "#F1F4F6"
  ink-text-secondary: "#CDD4DE"
  dark-outline: "#63716F"
  dark-outline-variant: "#34413F"
  light-mint: "#176448"
  light-primary-container: "#D7F0E5"
  light-secondary: "#385F71"
  paper-background: "#F5F7F8"
  paper-surface: "#FFFFFF"
  paper-surface-variant: "#E2E8EB"
  paper-text: "#151B22"
  paper-text-secondary: "#3D4854"
  light-outline: "#6B7975"
  light-outline-variant: "#CAD5D0"
typography:
  display-small:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "34sp"
    fontWeight: 600
    lineHeight: "40sp"
    letterSpacing: "-0.8sp"
  headline-large:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "30sp"
    fontWeight: 600
    lineHeight: "38sp"
    letterSpacing: "-0.6sp"
  headline-medium:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "26sp"
    fontWeight: 600
    lineHeight: "34sp"
    letterSpacing: "-0.5sp"
  headline-small:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "23sp"
    fontWeight: 600
    lineHeight: "30sp"
    letterSpacing: "-0.3sp"
  title-large:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "21sp"
    fontWeight: 600
    lineHeight: "28sp"
    letterSpacing: "-0.3sp"
  title-medium:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "23sp"
    letterSpacing: "0sp"
  title-small:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
    letterSpacing: "0sp"
  body-large:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
    letterSpacing: "0sp"
  body-medium:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "21sp"
    letterSpacing: "0sp"
  body-small:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: "18sp"
    letterSpacing: "0sp"
  label-large:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
    letterSpacing: "0sp"
  label-medium:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "18sp"
    letterSpacing: "0sp"
  label-small:
    fontFamily: "Manrope, Android glyph fallback"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "16sp"
    letterSpacing: "0sp"
rounded:
  artwork-small: "12dp"
  artwork-pinned: "14dp"
  row: "16dp"
  artwork-discovery: "18dp"
  content: "20dp"
  play-pause: "22dp"
  mini-player: "24dp"
  floating: "28dp"
  transport: "40dp"
  circle: "50%"
spacing:
  small: "8dp"
  medium: "16dp"
  large: "24dp"
  section: "32dp"
components:
  button-tonal:
    backgroundColor: "{colors.dark-primary-container}"
    textColor: "{colors.dark-container-content}"
    typography: "{typography.label-large}"
    height: "48dp"
  button-text:
    backgroundColor: "transparent"
    textColor: "{colors.dark-mint}"
    typography: "{typography.label-large}"
  search-field:
    textColor: "{colors.ink-text}"
    rounded: "{rounded.floating}"
    height: "60dp"
  home-search-action:
    textColor: "{colors.ink-text}"
    rounded: "{rounded.mini-player}"
    size: "48dp"
  stats-songs:
    backgroundColor: "{colors.dark-primary-container}"
    textColor: "{colors.ink-text}"
    typography: "{typography.display-small}"
    rounded: "{rounded.mini-player}"
    padding: "20dp"
  stats-artists:
    backgroundColor: "{colors.ink-surface}"
    textColor: "{colors.ink-text}"
    typography: "{typography.headline-large}"
    rounded: "{rounded.artwork-discovery}"
    padding: "20dp"
  pinned-artwork:
    backgroundColor: "{colors.ink-surface-variant}"
    rounded: "{rounded.artwork-pinned}"
    size: "64dp"
  pinned-playback:
    backgroundColor: "transparent"
    textColor: "{colors.ink-text}"
    typography: "{typography.title-medium}"
    padding: "8dp"
  navigation:
    textColor: "{colors.ink-text-secondary}"
    typography: "{typography.label-small}"
    rounded: "{rounded.floating}"
  navigation-active:
    backgroundColor: "{colors.dark-primary-container}"
    textColor: "{colors.ink-text}"
    typography: "{typography.label-small}"
  settings-container:
    backgroundColor: "{colors.ink-surface}"
    textColor: "{colors.ink-text}"
    rounded: "{rounded.content}"
  suggestion-chip:
    textColor: "{colors.ink-text}"
    typography: "{typography.label-large}"
    height: "48dp"
  onda-mark:
    textColor: "{colors.dark-mint}"
    size: "32dp"
  track-row:
    backgroundColor: "transparent"
    textColor: "{colors.ink-text}"
    typography: "{typography.title-small}"
    rounded: "{rounded.row}"
    padding: "8dp 0dp 8dp 8dp"
  transport-island:
    rounded: "{rounded.transport}"
    padding: "12dp 8dp"
    width: "420dp"
  play-pause:
    backgroundColor: "{colors.dark-mint}"
    textColor: "{colors.mint-content}"
    rounded: "{rounded.play-pause}"
    size: "72dp"
  play-pause-mini:
    backgroundColor: "{colors.dark-mint}"
    textColor: "{colors.mint-content}"
    rounded: "{rounded.play-pause}"
    size: "48dp"
  transport-step:
    backgroundColor: "{colors.dark-primary-container}"
    textColor: "{colors.dark-container-content}"
    rounded: "{rounded.content}"
    size: "56dp"
---

# Design System: Onda

## Overview

**Creative North Star: "Premium social music"**

Onda uses a dark content-first, editorial iOS-inspired hierarchy adapted to native Android. Immersive artwork and strong Manrope typography carry the visual emphasis. Ink and mint remain the stable identity; a playing cover can supply a readable accent. The original mark has three flowing waves.

Selective Liquid Glass gives floating playback and navigation controls a material boundary. Music rows and social content remain solid or cardless, with Bento composition reserved for discovery and statistics. Avoid excess cards, gradients, pills, shadows, icons and glass. Respect native Back behavior, touch targets, text scaling and system insets.

The approved LastWave adaptation supplies Home's statistics → collection selector → pinned playback/list hierarchy and the player's primary transport → lower modes hierarchy. Onda keeps its wave identity, typography, colors, adaptive navigation and selective glass. Attribution and the pinned GPL-3.0 reference are recorded in `THIRD_PARTY_NOTICES.md`; reference branding, cover artwork and audio are not app assets.

This is a record of the 0.4.0 production Kotlin/Compose system. Native Robolectric/Roborazzi fixtures in `.impeccable/review/` provide offline rendering evidence; they do not establish emulator/device, TalkBack, audio, haptic, frame or battery acceptance. The source tokens below use Android dp and scalable sp. Sidecar HTML/CSS is a browser-panel translation, not native acceptance evidence.

**Key Characteristics:**

- Dark content-first layouts with a complete light theme.
- Strong Manrope hierarchy and immersive artwork.
- LastWave-aligned Home collections and primary/secondary player hierarchy.
- Cover-derived accents with mathematical foreground contrast.
- Selective glass islands for navigation, playback and overlays.
- Cardless social layouts and truthful unavailable states.
- Native Android behavior with Reduced and Minimal effects.

## Colors

Ink and mint anchor the interface; the current cover contributes color without replacing readable surfaces.

### Primary

- **Dark mint / light mint:** primary actions, selected controls and the wave identity in their respective base themes.
- **Mint content:** foreground on dark mint; also the light theme's primary-container foreground.
- **Primary containers:** tonal actions and selected navigation backgrounds. Dark uses dark container content; light uses mint content. Secondary containers share these same values.
- **Cover accent:** runtime color, not a fixed swatch. `ArtworkTheme` applies it to the shell and full player. The extractor favors chromatic pixels over black, white and transparent borders, then adjusts the accent until it reaches at least 4.5:1 against the background, surface, surface variant and derived container. It chooses black or white for `onPrimary`, and checks readability throughout the color transition. The container mixes 18% accent into the surface and retains `onSurface` text.

### Secondary

- **Dark secondary / light secondary:** the cool secondary roles retained by the base theme. Dark secondary content is its dark foreground; light uses paper surface as foreground. These are theme roles, not a prescription to add decorative accents to every screen.

### Neutral

- **Ink background / surface / surface variant:** dark canvas, solid containers or glass tint source, and artwork placeholder surface.
- **Ink text / secondary text:** primary metadata and subordinate artists, descriptions and inactive labels.
- **Paper background / surface / surface variant:** light canvas, solid containers or glass tint source, and artwork placeholders.
- **Paper text / secondary text:** primary and subordinate light-theme content.
- **Outlines / outline variants:** boundary and divider roles. Messages uses an outline-variant divider; controls can retain native Material boundaries.

**The Content First Rule.** Keep surfaces and metadata readable; use the cover accent for atmosphere and action emphasis rather than coloring every container.

Missing or unreadable covers fall back to base brand colors. The source requests a software thumbnail (64px), analyzes off the UI thread and caches only 24 colors. Cancellation and a changed artwork URL prevent a stale result from recoloring the next track. Minimal returns the base theme before extraction or animation.

## Typography

**Display Font:** bundled Manrope variable font with Android glyph fallback.
**Body Font:** the same bundled Manrope; no secondary display family is introduced.

**Character:** semibold, slightly tightened large headings give the editorial hierarchy its emphasis. Regular body text and smaller medium-weight labels keep artists, explanations and control names subordinate without replacing them with icons.

### Hierarchy

- **Display small:** the emphasized recent-song count. **Headline large:** route headings, the Onda name, full-player title and recent-artist count. **Headline medium:** the Messages introduction. **Headline small:** queue heading.
- **Title large:** section names. **Title medium:** pinned playback, settings and queue track titles. **Title small:** discovery tiles, ordinary track rows and mini-player titles.
- **Body large:** full-player artists and substantial empty-state descriptions. **Body medium:** statistics labels, search text and supporting copy. **Body small:** compact artists, statistics scope and settings explanations.
- **Label large:** player context and Material action labels. **Label medium:** elapsed/remaining times and compact queue context. **Label small:** visible destination names and pinned/selected-row context.

The frontmatter records the ramp used by production screens. Display medium and large remain defined in `Theme.kt` but are not promoted into current screen rules. Font weights are supplied by the bundled variable font at 400, 500, 600 and 700; current custom text roles use 400, 500 and 600. All source sizes scale in sp.

Do not impose fixed metadata heights. The full player scrolls, while tiles and rows use explicit wrapping and overflow limits for bounded collections. Destination labels stay visible and announce selection through semantics as well as color.

## Layout

The shared spacing rhythm is small, medium, large and section; component gaps also use small source-specific steps. Home has a centered content bound (760dp); the full-player column is bounded (460dp), cover (380dp), and independent transport island (420dp). The bottom mini-player and dock are bounded (640dp).

Compact windows use a floating bottom dock. A floating left rail appears only when the window is both wide enough (840dp) and high enough (600dp); its allocation is 104dp, or 136dp at 150%+ font scale. The mini-player remains below the content. IME visibility hides dock/rail and reserves keyboard space. Content is clipped to its actual remaining viewport, with safe drawing and navigation-bar insets applied.

At 150%+ text, a dock under 340dp uses two columns; other docks below 640dp use three. At 640dp or wider it retains five columns. In a window shorter than 500dp with 150%+ text, it becomes one horizontally scrollable row of 140dp destinations; the selected destination requests bring-into-view. This protects content height while keeping every destination reachable.

Home starts with a compact wordmark/search row. When local history exists, Recent listening shows an asymmetric solid statistics Bento: song and known-artist tiles have width weights 1.4:1 and a 12dp gap. The counts describe at most 20 recent unique songs on this device; known artists are deduplicated by artist ID after blank names are removed. When no known artists are available, the value is a dash.

Your mix has a Recently played / Recommended selector, the existing current track pinned first, then plain collection rows. Recently played is the default when history exists and is unavailable without history. The pinned track is excluded from the ordinary rows and discovery tiles. Collections show four rows until Show all is chosen; collection changes reset that expansion. Discovery follows the list, then the cardless friend state. Home has no featured hero or horizontal history-artwork rail.

Statistics, the collection heading/selector and discovery stack when their available width is under 320dp or font scale reaches 150%. The normal discovery arrangement is unequal: a larger left artwork tile and smaller tiles to the right. Bento geometry belongs to discovery and statistics, not every list or social section. Home uses a 20dp inter-section gap with 24dp content gutters and 16dp row gutters.

## Elevation & Depth

Depth combines tonal surfaces with captured-background blur, a directional edge and limited native elevation. Dense content is flat or cardless. The quiet static wave backdrop is the capture source; moving foreground content and other glass surfaces are excluded. Each Android window owns its capture state. Haze 1.5.4 supplies blur, tint and edge light; optical refraction is not implemented.

### Shadow Vocabulary

- **None / Subtle:** native elevation (0dp).
- **Standard:** native elevation (2dp); search surfaces use this material.
- **Elevated:** native elevation (8dp); dock, rail, mini-player and transport islands use this material.
- **Overlay:** native elevation (10dp); menus, queue and confirmation overlays use their own window scope.

Full material uses the exact tint/edge/blur matrix in `.impeccable/design.json` and `GlassTokens`. The edge is a diagonal gradient with a density-independent stroke (1dp), not a general-purpose decorative gradient. There is no transferable CSS box-shadow value for Compose shadow elevation.

Captured blur is enabled on API33+ devices that are not low-RAM. Earlier Android versions, low-RAM devices and Minimal use an opaque surface fallback. Reduced uses 8dp blur and .94 tint with zero elevation, while retaining each material's edge value. None stays opaque with no border, blur or elevation in every effect mode. Minimal also removes wave decoration, artwork extraction and nonessential haptics.

**The Selective Glass Rule.** Use glass for floating navigation, playback controls, the observed search entrance/field and appropriate overlays. Keep social copy, music rows and dense settings opaque or cardless; never capture another glass surface into a backdrop.

The implemented cover-color transition is 220ms in Full and instantaneous in Reduced. Minimal skips it. The static wave backdrop has no continuous animation. No custom navigation or player/shared-element transition timing is established by this build.

## Shapes

Artwork uses gently curved rectangles: small covers, pinned playback covers, discovery covers and full-player covers take their observed roles from the frontmatter. Rows have softer clipping without becoming bordered cards. Settings use a solid content container. Statistics use solid unequal tiles with 24dp song corners and 18dp artist corners; the mini-player, floating surfaces and broad transport island retain their observed rounded edges.

Play/pause is a rounded square (22dp radius): 72dp in the full player, 48dp in the mini-player and pinned Home row. Primary previous/next buttons are tonal rounded squares (56dp, 20dp radius). The compact Home search action is 48dp with 24dp corners. The selected navigation icon has a compact rounded highlight (48 × 30dp, 18dp radius) inside a destination target of at least 56dp high. Profile retains its circular avatar treatment. These purposeful controls do not authorize pill-shaped treatments for ordinary content.

The Onda mark is three flowing strokes with round caps. Wave contours remain restrained and static; Minimal removes the backdrop contours while retaining the identifying mark.

## Components

### Buttons

Native Material3 tonal and text buttons retain recognizable Android behavior. Tonal buttons use secondary-container roles, which match primary-container colors in the base and artwork themes. The filled play/pause rounded square uses primary/onPrimary; primary previous/next use tonal containers. Explicit content actions and Messages' text action have a minimum height (48dp); Material icon controls retain native minimum interactive sizing. Native states and accessible action labels remain in Compose rather than acquiring custom web hover animations.

Deliberate play, previous/next and destination actions wrapped by `tactileAction` request Android `CLOCK_TICK` feedback, respecting the system setting. Minimal disables that feedback. Seeking and scrolling do not request continuous haptics. Shuffle and repeat expose their state; do not imply every control has custom haptics.

### Chips

Explore's suggestion chips provide real genre search actions when the query is blank. They use native Material3 suggestion-chip styling and explicit minimum height (48dp), with an 8dp flow gap. Their task-specific presence does not make pills the default shape for metadata.

### Cards / Containers

Home's statistics Bento uses solid primary-container and surface tiles with 20dp internal padding and a 4dp value/label gap. Its mix list, discovery tile framing and social section are cardless. Ordinary Home rows remain unselected; the existing current track is a separate pinned plain row with 64dp artwork, a 48dp play/pause action and visible “In player” text. Opening its metadata opens the player. In other collections such as Explore, a selected `TrackRow` gets a 45%-alpha primary container, accent title and visible state. Profile's effects selector is an opaque settings container with content corners; keep these functional groupings distinct from decorative cards.

### Inputs / Fields

The Explore input sits inside Standard glass with floating corners and a minimum height (60dp). Native field container/indicator colors are transparent so the surrounding material is the boundary. A persistent semantic search name accompanies placeholder copy; a clear action appears when text exists. IME search dismisses the keyboard. Home's search entrance is a compact 48dp Standard-glass icon action in the identity row with 24dp corners and an accessible search name.

### Collection selector

Your mix uses a native TextButton with visible Recently played / Recommended text, a 20dp expand icon and a minimum height (48dp). Its dropdown owns an Overlay glass scope. Menu options expose selected semantics; Recently played is disabled without actual history. At large text or narrow content width the title and selector stack.

### Navigation

Home, Explore, Library, Messages and Profile use outlined native icons plus visible label-small names. Active tabs have the compact container highlight, a brighter label and selected semantics. Preserve the dock/rail, large-text and short-window behavior in Layout. Mini-player metadata opens the full player; its play/pause and next controls remain separate accessible actions.

### Playback islands

The full player gives the cover and metadata visual priority. Progress and elapsed/remaining labels are cardless; the remaining label carries a minus sign visually and a “time remaining” accessibility description. The separate Elevated island contains only the three primary previous/play-pause/next actions. A cardless lower row places shuffle and repeat around a real queue-count TextButton with a minimum height (48dp). Active modes expose their state through labels/semantics and accent color.

Seek is disabled when duration is unavailable and reports its position/duration state without a repeating live announcement. Queue opens a separately scoped overlay; metadata and controls scroll at large text sizes. Mini-player artwork is compact (48dp) and its progress strip is decorative to accessibility services. The Home pinned player and mini-player use the same 48dp, 22dp-corner play/pause control as the larger 72dp full-player action.

### Social and unavailable states

Friend activity is quiet cardless copy. Messages uses a route heading, divider, conversation-oriented explanation and real discovery action. Saved-library, account, friend and messaging backends are absent: render accurate empty states without fake people, unread counts, messages or send controls. Local history is actual listening continuity, not evidence of a social backend.

**The Truthful State Rule.** Give empty, loading and failure states readable copy and real recovery/discovery actions; never use fabricated activity to fill a layout.

## Do's and Don'ts

### Do:

- **Do** lead with music artwork and readable Manrope hierarchy.
- **Do** keep ink/mint as the fallback and let the current cover color the documented action roles with contrast checks.
- **Do** use the original three-flowing-wave mark and restrained static backdrop contours.
- **Do** preserve visible destination names, selected/action semantics, native Back behavior and system/IME insets.
- **Do** honor 48dp controls, scalable text through 200%, Reduced and Minimal effects.
- **Do** keep local listening and unavailable social states truthful.
- **Do** label native host fixtures separately from pending device, TalkBack, audio, haptic, frame and battery verification.

### Don't:

- **Don't** add excess cards, gradients, pills, shadows, icons or glass.
- **Don't** extend Bento composition beyond discovery or statistics.
- **Don't** blur dense metadata, social copy or settings, or capture glass into another glass source.
- **Don't** replace actual content with fake social activity, messages or unsupported action controls.
- **Don't** invent custom navigation/player motion, optical refraction or physical acceptance claims.
- **Don't** promote browser-side preview ramps or translated snippets into new native design tokens.
