# Onda architecture

The active Android app is a LastWave derivative; provenance and GPL notices are in UPSTREAM.md and LICENSE. The earlier independent foundation is retained in foundation/ and excluded from the active build.

## Android

The imported app retains its catalogue, library, Last.fm integration, YouTube resolution, local downloads, native audio, foreground playback service and single MusicPlayer. Internal com.lastwave.app namespaces stay stable for JNI and existing integrations; the install ID is dev.socialmusic.app. Circle is a fourth main tab and invokes the same player through validated track metadata, never another user's stream URLs or cookies.

The social package owns Onda email accounts, encrypted Android Keystore-backed sessions, directed follows, friend listening, opt-in taste influence and privacy controls. Public project configuration comes from environment variables or ignored local properties. HTTP cancellation cancels the underlying request; account generations reject results from old sessions. Logout clears local identity and attempts bounded server revocation.

SocialListeningPublisher observes coarse player state. Sharing starts disabled. It publishes state changes and 30-second heartbeats with a server-enforced 90-second live expiry. A meaningful listening signal requires 30 uninterrupted seconds, an enabled collection purpose, and no private session. Stable event IDs make retries idempotent. The Circle UI refreshes every 30 seconds and discards permission-bearing results when authorization cannot be refreshed.

## Supabase

Supabase owns authentication, profiles, consent, follow/block relations, listening presence, private events and explicit influence selections. RLS permits scoped reads; writes go through narrow authenticated RPCs deriving ownership from auth.uid(). Functions fix their search_path and deny anonymous execution. Shared listening is followers-only in V1. Raw listening events remain owner-only.

Consent and relation changes serialize with a short transaction advisory lock. This prevents an in-flight enable request from restoring a choice after blocking, unfollowing or consent revocation. Listening writes hold a settings-row share lock so preference changes cannot race collection/publication. The control lock is an initial correctness choice; replace it with a measured graph-lock strategy if control-write load justifies it.

## Recommendation service boundary

POST /functions/v1/recommendations accepts the caller's access token and returns schema_version=1, config_version, policy_version, generated_at and canonical items with explanations. The endpoint obtains a permission-filtered recommendation_context under the caller's JWT. It uses no privileged service key and rechecks source eligibility and policy version after ranking. Gateway JWT verification is disabled because the Data API performs token validation and the RPC rejects anonymous callers; the handler rejects missing tokens explicitly.

The pure ranker preserves personal evidence and gives all explicitly selected, eligible people a combined maximum weight of 0.10. Inputs are shared taste inferred from consented meaningful listening and feedback, never another person's recommendation feed. Current candidates are limited to consented event metadata; catalogue discovery, diversity and learned embeddings remain later work.

The same HTTP contract can be implemented in a Python service later. Supabase continues to own authorization and transactional social state; the ranking service does not become an authority for privacy. Advanced model infrastructure is unnecessary for this first implementation.

## Scope and verification

Messaging, mutual/follower management views, live Realtime subscriptions, external app links, recovery/deletion flows and production delivery are subsequent increments. Android build evidence and remaining device/server limits are recorded in BUILD_REPORT.md. Historical broader product plans remain in DATABASE.md, RECOMMENDATIONS.md and ROADMAP.md; implemented SQL is the source of truth for current V1 behavior.
