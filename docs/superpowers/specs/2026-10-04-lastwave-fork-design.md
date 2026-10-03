# Onda: LastWave fork and native social layer

Date: 4 October 2026 (Asia/Calcutta).
Status: the user directed implementation on 4 October 2026 after reviewing the product direction and backend split. Implementation proceeds in the current checkout; completion is recorded by build and test evidence.

Revision, 4 October 2026: the user requested new-profile defaults of personalized recommendations, listening sharing and taste sharing **on**, with private session **off**. This overrides earlier default-off wording for those three account switches. Existing user choices remain stored. Per-person recommendation influence still requires the receiver's explicit choice.

## Product direction

The user explicitly changed the approach from independently rebuilding a music player to directly reusing the whole LastWave application and making social music the differentiator. They authorized cloning LastWave and using its repository at our discretion. This supersedes the earlier brief's restrictions against copying its implementation and layouts. Onda remains the product name.

Success means retaining LastWave's music experience while adding Onda accounts, profiles, follows, privacy-controlled listening activity, one-to-one conversations, and playable music shares. On 4 October the user specified Spotify as the reference for following and viewing friends' listening, and requested explicit influence from selected people on the user's own recommendations. They then clarified the input: **their shared music taste**, rather than their actual recommended songs. Social features must help people discover and discuss music; they must use the existing global player.

## Source and evidence

- Repository: https://github.com/Clash-Projects/LastWave-Native
- Branch inspected: `main`.
- Pinned commit: `3156a434d7e798a02df7e5fd47ae13f10a9c983a`.
- Complete shallow clone: `.tools/lastwave-reference/`, including its Git checkout and 603 tracked files. It is an ignored reference checkout, not yet Onda's active build.
- The inspected checkout is clean.
- Upstream `LICENSE` contains GNU GPL version 3. Preserve it and existing copyright and third-party notices. Mark Onda's modifications and dates. Distribution of the derived Android application must follow GPL requirements, including corresponding source availability. Do not represent Onda as the official LastWave application.
- Upstream code contains a working implementation surface, but functionality has not been exercised on a device here. Documentation claims are not test evidence.
- A baseline Gradle configuration attempt failed while downloading Gradle 9.3.1: network timeout. Neither configuration, compilation, tests, nor a debug APK have been verified for this source.

## Selected approach

Make a source-level LastWave derivative. Reuse its app, resources, native audio library, catalog/search, player, lyrics, playlists, downloads, and UI rather than porting these systems into Onda's small Phase 1 modules.

Preserve the current Onda foundation in Git history and an explicitly labeled foundation directory during migration. Import the upstream source into the active root build with a provenance file recording the repository, commit, import date, and exclusions. Keep the upstream license and attribution. Exclude upstream Git metadata, generated logs, development scratch directories, and the redundant `reference zip/extracted/LastWave` legacy project from the active build. Preserve the untouched full reference clone for comparisons.

Alternatives considered: keeping the independent Onda engine would duplicate work the user now wants reused; selectively copying a few music packages would require adapting substantial interdependent code. Both conflict with the requested whole-app reuse. A direct derivative is selected.

## Fork identity and release boundaries

Set the install identity to `dev.socialmusic.app`, retaining Kotlin package `com.lastwave.app` initially to minimize import/JNI/resource breakage. Android namespace and install application ID need not match. Use explicit fully qualified manifest component names when changing the install identity. Audit provider authorities, custom permissions, widget declarations, and callback links for collisions.

Replace visible product labels and launcher identity with Onda, preserving the upstream identity in attribution and source notices. Keep internal native library/JNI names until a separate verified rename is justified. Do not alter upstream copyright notices.

Disable upstream release polling in `data/update/AppUpdateManager.kt`: the inspected implementation queries LastWave's GitHub releases and opens its APK download. Onda must never prompt its users to install another application's update. Enable Onda updates only once Onda has an actual release channel.

Do not inherit upstream signing credentials, embedded service secrets, or release automation. Provide public configuration examples only. Release signing must use Onda-owned configuration, with no fallback that silently produces a publicly distributed debug-signed release. Audit backup rules before introducing Onda sessions or private message caches.

## Music reuse and integration seams

The inspected source exposes `playback/MusicPlayer.kt`, its process-wide singleton, `state`, `chromeState`, `PlayableTrack`, and `play(...)`/`playQueue(...)`. Playback is already coordinated with `MusicPlaybackService`. Preserve this ownership model rather than introduce the foundation's proposed second player abstraction alongside it.

Use `ui/shell/MainShell.kt` and `ui/navigation/NavGraph.kt` to integrate Onda's social entry point. Existing Last.fm `FriendsScreen`/`FriendProfileScreen` remain integration features and must not be treated as Onda account identities. Last.fm and YouTube connection states remain distinct from Onda's Supabase session. Guest music listening remains available without creating an Onda account.

Onda music attachments carry a versioned provider identity and display metadata. A YouTube attachment contains a video ID; future providers require explicit adapters. Convert an attachment to the existing `PlayableTrack` only at the playback boundary and resolve streams through the existing engine. Never serialize `playbackUrl`, local filesystem paths, cookies, or session tokens into social content. Unsupported/local-only music is shown as unavailable for internal sharing with an actionable message.

## Social architecture

Add cohesive `social` packages for models, repositories, persistence, and UI inside the derivative. Use the existing Hilt and lifecycle-aware Compose patterns. Keep backend access out of Composables. Retain useful existing Onda provider-independent models by adapting them at one boundary; do not add competing copies of every model.

Supabase Auth supplies Onda user UUIDs. Use email authentication first, session refresh, explicit sign-out, unique normalized usernames, and editable display names. Last.fm usernames are optional profile integrations, never authentication proof for Onda.

An unconfigured backend leaves music operational and displays an honest social setup state. Network failures offer retry; empty friend lists and conversations show actual empty states. Do not insert fabricated friends, messages, listening activity, or successful sends. A real Supabase URL and public publishable key are external inputs needed to verify account and cross-device flows; no service-role key belongs in Android.

### Profiles, relationships, and privacy

Use Spotify's profile-search and directed-follow interaction as the reference: search people, open a profile, Follow/Following, view followers/following, remove a follower, and block/unblock through profile actions. Keep Onda's typography and existing derivative design system. A removed follower may follow again unless blocked. No mutual follow is required to follow a person.

Implement separate visibility settings for profile, now-playing, activity, and shared playlists. Defaults for listening activity and presence remain private. Following someone does not automatically authorize broadcasting their listening information or using their recommendations.

Enforce visibility and blocks in PostgreSQL policies/functions before exposing data. Client filtering improves presentation but cannot authorize access. Purge cached social information and re-authorize channels when access changes.

### Friends listening

Use Spotify's current mobile listening-activity behavior as the reference: a row of friends with current or last-played tracks, with the same activity also accessible from a conversation's person header. Track details offer playback and save actions. Show live playback and last-played activity as distinct states; a historical track must not receive a live indicator. Showing last-played metadata requires separately authorized activity visibility after live presence expires.

Provide a master listening-sharing switch, off by default, audience selection, and a private-session action that suppresses publishing. Viewing eligible friends does not require the viewer to share their own listening. Allow hiding a person's listening activity without changing the follow relationship, messaging eligibility, or explicit recommendation-influence preference. Reactions and shared listening are separate increments rather than requirements to finish following/activity.

Subscribe to the existing player's coarse state rather than its progress ticker. Publish only after an explicit opt-in, on track/playback changes plus a throttled heartbeat. Server timestamps and expiry govern freshness; paused/offline/expired listeners are not presented as live.

Activity cards show the profile, track, and relevant time. Tapping play uses the same `MusicPlayer`. Player failures remain visible through existing playback state; social publishing failures must never stop audio.

### Explicit influence from selected people

Onda's requested addition is a distinct per-person action, **Influence my recommendations**, available on followed profiles and in a recommendation-settings list of selected people. It defaults off for every person, including immediately after following. Merely viewing a profile, seeing listening activity, receiving a song, or having a mutual follow must not enable it. The receiver explicitly adds or removes each source person. The first version needs a switch, not an unrequested strength slider.

The user explicitly selected **their shared music taste** as the input. Use permissioned artist, genre, and track affinities derived from that person's own listening, likes, and saves. Do not copy their personalized recommendation feed or infer that exposure to a recommended song means they listened to or liked it. Missing signals remain missing; sparse permitted taste must produce reduced or no influence rather than invented preferences.

The source person separately controls whether and to whom their taste signals may be shared. Sharing listening activity does not imply permission to reuse private taste. Enabling influence for an ineligible source shows why it is unavailable and leaves it off. Do not send an unsolicited request or notification on the receiver's behalf.

Generate a normalized contribution only from the receiver's enabled sources that still satisfy source-sharing permission, follow eligibility, and both directions of blocking. Use shared taste to propose relevant candidates and rank candidate affinity; the source person's generated recommendation feed is not an input. Blend this contribution into the receiver's recommendations while keeping the receiver's own taste dominant. Use the existing V1 social budget of 10% as an initial centrally configured assumption. Split that total across eligible selected people; following more people must not multiply the budget. If there are no enabled eligible sources, their contribution is zero and the remaining personal weights normalize to one.

Keep this influence as a removable ranking layer, not an overwrite of the receiver's taste profile. Listening, liking, or saving an influenced recommendation can subsequently create ordinary first-party evidence. Removing an influence removes its imported contribution and cached attribution; it need not erase the user's own independently recorded listening or likes. Preserve other eligible candidate reasons if a track is also supported by the receiver's own taste.

Label affected recommendations with truthful attribution such as **Inspired by Maya's music taste**. Let the user manage or remove the source from that explanation. Name a track or artist affinity only when the source authorized that detail; otherwise use a general shared-taste reason. Never attribute personal taste or listening behavior to someone merely because a track appeared in their generated recommendations.

Do not propagate already socially influenced recommendations recursively as if they were the source person's own taste. Enforce deduplication, artist diversity, explicit dislikes/exclusions, availability checks, and configured content preferences after combining candidates. Unfollow, block, source consent revocation, account deletion, or disabling the switch removes eligibility before subsequent serving; re-following or unblocking leaves the switch off until explicitly enabled again. Failed refresh must not serve stale revoked contributions.

### Messaging and music sharing

Start with one-to-one conversations. Create a conversation through a transactional RPC that canonicalizes the user pair and checks eligibility; clients cannot insert arbitrary members. RLS restricts messages to conversation participants and prevents forged senders or conversation changes.

Support text and track attachments first. Shared tracks show artwork, title, artist, a play action, and an unavailable state if metadata or provider resolution fails. Share from the full player and track context menu, choose an eligible recipient, then enqueue an idempotent message. Both sent and received cards use the existing global player.

Persist outgoing messages with a stable UUID and Pending/Sent/Failed status. Retry retains the UUID. Realtime signals trigger reconciliation rather than act as the only history source. Fetch paginated history using `(created_at, id)` cursors and deduplicate messages by UUID. Logout cancels subscriptions and purges account-bound tokens, caches, and pending work.

Typing and read receipts follow only after text/track send, receive, pagination, reconnect, and authorization work. Group messaging and synchronized listening remain later milestones.

## Backend deliverables

Supabase is the source of truth for transactional/social data and authorized recommendation events, preferences, and aggregates. Define a versioned authenticated recommendation HTTP contract before coupling Android to ranking implementation. Start with bounded server-side rule ranking; a later Python service can implement the same contract and keep PostgreSQL storage. Derive identity from verified Supabase JWTs, not a request user ID. Backend workers may precompute permitted taste; privileged access must explicitly enforce eligibility. Heavy model work runs outside the hosted Edge runtime and outside message/playback write transactions. Recommendation failure falls back to an honest independent music discovery experience without exposing stale revoked social contributions.

Turn the relevant portions of `DATABASE.md` into versioned SQL migrations: profiles, settings, follows, blocks, direct conversations/membership, messages, expiring now-playing, and per-person recommendation influence. The influence record is keyed by `(receiver_id, source_user_id)` and stores the receiver's explicit choice; only that receiver can mutate it. Store the source's audience permission separately. Enforce source eligibility at candidate generation and serving, rather than trusting the preference record alone. Enable RLS before client grants. Use narrow RPCs for cross-row invariants with qualified SQL names and restricted execution permissions.

Add database integration tests for outsiders, forged sender/member IDs, repeated/concurrent pair creation, invalid attachments, both directions of blocking, visibility changes, and expired presence. Account/profile media may use Storage with owner-scoped access; the backend must not host a commercial music catalog.

Do not deploy migrations or create external accounts without access to the user's actual target project and authorization for that deployment.

## Incremental execution and acceptance

1. **Verified music fork:** import and provenance, license/attribution, Onda identity, update-channel isolation, portable tooling/CI alignment; configure, test, build a debug APK, and run lint. Test guest entry, playback, background controls, search, and library on a device before claiming preserved behavior.
2. **Onda identity and relationships:** SQL/RLS migrations plus email auth, profile setup/search, follows, blocks, privacy controls, and account cleanup. Verify with multiple actual accounts.
3. **Conversations and playable shares:** direct-chat RPC, pagination/outbox/reconciliation, UI and share entry points; test outsider denial, duplicate retries, reconnect, missing tracks, and uninterrupted global playback.
4. **Opt-in friend listening:** state publisher, expiry/visibility policies, friend activity presentation, play actions, and privacy-revocation tests. Real cross-device evidence is required.
5. **Selected-person taste influence:** per-person selection and source permission, shared-taste candidate/affinity adapters, normalized blend, removable provenance, explanations, and settings. Tests must establish zero influence before selection; bounded influence with multiple selected people; sparse/missing taste; deduplication and hard exclusions; exclusion of the source's generated recommendation feed; no recursive social propagation; and immediate removal of imported evidence after disabling, unfollowing, blocking, or source consent revocation. Persisted personal evidence remains valid after removal.

Each increment gets a focused implementation plan and its own verification record. Importing the source does not mark the social system complete.

## Build constraints discovered

The inspected main branch pins Gradle 9.3.1, AGP 9.1.1, Kotlin 2.4.10, KSP 2.3.10, Hilt 2.59.2, and Compose BOM 2026.06.01. The app compiles against SDK 37; the USB module declares SDK 36; the app's default minimum is API 29. Native audio configuration requests CMake 3.22.1 and fetches pinned soxr source. These differ materially from Onda's verified SDK 35/Gradle 8.11.1 foundation.

Verify artifact availability, supported JDK/NDK/SDK tooling, native build inputs, and a reproducible upstream build before adopting or adjusting versions. Do not silently downgrade dependencies or remove native features to claim a full import builds. If upstream main cannot be made reproducible, report the precise failure and compare a named upstream release before choosing a different source commit.

## Review decisions

The chosen design is a GPL LastWave-derived Onda app, preserving its music implementation and adding first-party social identity, Spotify-referenced follows/listening activity, conversations, playable shares, and explicitly requested influence from selected people's shared music taste. The user has confirmed the shared-taste input. Supabase credentials and device/server verification remain external prerequisites. This document seeks review of the concrete migration and social boundaries, rather than permission to clone, which was already given and completed.

## Spotify references

Reviewed official documentation on 4 October 2026. These are behavior references, not a claim that Onda connects to Spotify accounts or imports Spotify's private recommendation algorithm.

- [Follow your friends and manage followers](https://support.spotify.com/bd-en/article/follow-friends-manage-followers/): profile search, directed follows, follower management, and blocking.
- [Listening Activity](https://support.spotify.com/us/article/listening-activity/): current/last-played tracks, mobile activity and conversation entry points, playback/save actions, default-off sharing, and audience controls.
- [Listening Activity and Request to Jam in Messages](https://newsroom.spotify.com/2026-01-07/listening-activity-request-to-jam-messages-updates/): includes the September 2026 update separating hiding someone's listening activity from messaging and following.

The per-person recommendation-influence action is Onda's requested design; it is not described here as a Spotify feature or as a verified market-unique claim.
