# Database and authorization plan

Status: server schema/policies below are a design, not deployed migrations. Phase 1 implements only a Room catalog cache. Supabase project access is required before server integration can be verified.

## Identity and constraints

Use PostgreSQL UUID primary keys, `timestamptz` server timestamps, foreign keys, bounded text and JSON payload validation. Profiles reference `auth.users` and have a case-normalized unique username. Catalog references use `(provider, provider_id)` and versioned metadata snapshots; stream URLs are not stored. Index every foreign key used by policies. Do not rely on Android-generated owner IDs for authorization.

| Table | Key relationships / constraints | Primary access |
| --- | --- | --- |
| profiles | PK user_id; unique normalized username; bounded bio | Owner write; read obeys profile visibility and block rules |
| user_settings | PK user_id; individual visibility enums, opt-ins | Owner only |
| follows | PK (follower_id, following_id); no self-follow | Related users; insert/delete by follower with blocks checked |
| blocked_users | PK (blocker_id, blocked_id); no self-block | Blocker only; server helper checks both directions |
| recommendation_influences | PK (receiver_id, source_user_id); no self-source; explicit enabled preference, default off | Receiver reads/writes own selections; source eligibility checked against follows, source taste-sharing audience and blocks |
| conversations | UUID; unique canonical participant pair for direct chat | Members only; transactional creation RPC |
| conversation_members | PK (conversation_id, user_id); exactly two for direct chat | No arbitrary client writes; authorized membership helper |
| messages | UUID idempotency key; conversation_id/sender_id; kind; versioned payload; optional reply_id | Members read; authenticated sender inserts if interaction allowed |
| message_reads | PK (conversation_id, user_id); watermark_message_id | Member writes own receipt; watermark must belong to conversation |
| user_presence | PK user_id; track_ref; status/position; server_time/expires_at | Owner updates; visibility and expiry constrain reads |
| listening_activity | user_id; entity_ref; event kind; timestamp | Owner insert; separate activity/history visibility |
| playback_events | user_id; stable event UUID; session_id; signal/track/time/context | Owner/backend only; no tick events |
| recommendation_feedback | user_id; impression/event UUID; action/config_version | Owner/backend only |
| taste_profiles | PK user_id; versioned affinity aggregates; updated_at | Owner read; trusted aggregate writer |
| playlists | owner_id; title; visibility; version | Owner writes; reads obey visibility/blocks |
| playlist_tracks | occurrence UUID; playlist_id; entity_ref; position | Authorized playlist readers/writers; transaction for ordering |
| liked_tracks / liked_albums | PK (user_id, provider, provider_id) | Owner only initially |
| reports | reporter_id; target_type/id; bounded reason | Reporter insert/read own; moderation service reads |
| notifications | recipient_id; kind; bounded entity refs/read_at | Recipient read/update restricted read_at |
| device_tokens | user_id; device/token fingerprint; platform | Owner registration; trusted delivery service |

Playlist members and shared listening tables are deferred until collaboration exists. No empty table merely for a future feature.

## Required indexes

`messages(conversation_id, created_at DESC, id DESC)`; `conversation_members(user_id, conversation_id)`; `listening_activity(user_id, created_at DESC, id DESC)`; `playback_events(user_id, created_at DESC)`; `playlist_tracks(playlist_id, position, id)`; `follows(following_id, follower_id)` in addition to its composite primary key; `blocked_users(blocked_id, blocker_id)` in addition to its primary key. Add recipient/time index for notifications and provider ID uniqueness for metadata. Page messages/activity with stable tuple cursors, not an unbounded select.

## RLS and write paths

Enable RLS on every exposed table before grants. Explicitly constrain `authenticated` and reject null auth identity. Policies apply both USING and WITH CHECK to prevent ownership changes. Messages require membership for SELECT; INSERT also requires `sender_id = auth.uid()`, live interaction permission, bounded validated attachment and valid reply target. Messages cannot be moved to another conversation. Edits, deletion, membership changes and reads use narrow RPCs where cross-row invariants are involved.

Conversation creation, playlist ordering and account deletion are transactions. Two-party conversation creation canonicalizes the pair, checks both users, privacy and blocking, then creates conversation+members atomically with race-safe uniqueness. Direct client member inserts/updates are denied. Existing conversation history may remain readable to its members after a block; new messages, typing, presence and recommendation contributions are denied. Blocking is not retroactive deletion of already received messages.

`can_view(subject, viewer, surface)` evaluates the selected visibility and directed relationships: Followers means viewer follows subject; Mutuals requires both directions. Owner may read own row. Blocks deny cross-user access even for Everyone. Profile visibility never automatically exposes history. Backend eligibility is repeated at candidate generation and final recommendation serving.

Selected-person recommendation influence uses shared music taste, as clarified by the user on 4 October 2026. Add a distinct taste-sharing consent/audience to `user_settings`; profile or listening visibility never grants taste reuse. `recommendation_influences` stores only the receiver's explicit selection. RLS prevents selecting/mutating on another receiver's behalf. A trusted backend derives permitted artist/genre/track affinities without exposing raw private taste vectors or the source's generated recommendation feed. Check receiver selection, current follow, source audience consent, and both directions of blocking during candidate generation and serving. Removing influence, unfollowing, blocking, or revoking source consent invalidates cached imported signals and explanations. Re-follow/unblock must not automatically restore selection. Keep the receiver's independent listens/likes/saves separate from removable imported scores.

Avoid recursive RLS by using narrowly scoped membership/visibility helper functions in an unexposed schema. If SECURITY DEFINER is necessary, set an empty/fixed search_path, qualify tables, bind decisions to auth.uid(), revoke default execution grants and audit permitted callers. Do not expose a generic function that accepts an arbitrary viewer to leak private relationships. Public views must preserve caller RLS (security invoker) or remain unavailable to client roles. Service-role bypass is never an Android capability.

Realtime table events respect authorized database reads; private Broadcast/Presence channels require their own membership authorization. Guessing a conversation topic is not membership. On reconnect or block/privacy change, unsubscribe/re-authorize and purge stale social data. FCM/push delivery uses a trusted function, never a client-held secret or private message text in analytics.

Storage buckets hold allowed avatars and playlist artwork, not commercial audio. Owner-prefixed object keys, MIME/size restrictions and Storage RLS govern writes and reads. Private files use expiring authorized URLs. Never assume a private table makes a public bucket private.

## Mandatory adversarial test matrix

Run SQL integration tests with four users (A/B members, outsider C, blocked D) and an anonymous role. Authenticate JWT subjects rather than executing every test as postgres/service role.

- C and anonymous cannot SELECT, INSERT, UPDATE or DELETE A/B messages, even with known IDs.
- C cannot add itself to A/B membership; A cannot add a third member or forge sender B.
- Sender cannot move a message or reply to a message in another conversation.
- Pair creation repeated/concurrent returns one authorized direct conversation.
- A cannot change owner_id or use UPDATE to escape INSERT constraints.
- Every visibility surface is tested for owner, follower, mutual, unrelated and blocked viewers.
- A follow alone never enables recommendation influence; another user cannot forge a receiver's selection or obtain private source taste by inserting a preference. Source consent/follow/block revocation invalidates imported scores and attribution, including cached results; re-follow/unblock does not restore selection.
- Blocking prevents new messages/follows/presence/social contributions in both directions; prior member history follows documented retention rules.
- Private playlist_tracks are unreadable through joins or direct requests; reordering is atomic.
- Feedback/taste/events/settings/device tokens are private, including via views/functions.
- Cross-conversation receipts and unauthorized private-channel joins fail.
- Storage path spoofing and public-access bypass fail.

A policy is not complete until these run against a real database. Android unit tests cannot establish RLS correctness.

## Local storage and retention

The foundation excludes all application data from cloud backup and device transfer through explicit Android 12+ data-extraction rules and legacy full-backup rules. This complements the older allowBackup setting; it avoids implicitly transporting future sessions or account caches. This is configuration verification, not a device backup/restore test. See [Android backup guidance](https://developer.android.com/identity/data/autobackup).

Room v1 stores bounded metadata cache rows keyed by canonical music ID, with unique provider identity and cached_at index. DAO exposes a limited newest-first Flow and upsert. No destructive migration fallback; export schemas and introduce tested migrations when version changes. Cached metadata never authorizes playback or reveals private account data.

Later account-partitioned tables hold library/message history/outbox with stable IDs, server versions, Pending/Synced/Failed state and cursor watermarks. Logout cancels workers/channels and purges private caches/tokens. Reconcile updates by version; retries retain the original mutation UUID. Server retention/deletion jobs must remove derived taste/social data and invalidate cached explanations alongside original user data. Initial planned raw recommendation-event retention is 90 days, configurable with a documented deletion path and consent settings before collection starts.
