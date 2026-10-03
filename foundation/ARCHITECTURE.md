# Architecture

Status: proposed product architecture plus Phase 1 foundation scope. Music is the shared object across catalog, player, library, recommendations, activity and conversations.

## Decisions and alternatives

Use a new Kotlin Android repository with a few real module boundaries. Extending VoiceNotes would couple unrelated runtimes and data. Forking LastWave would accelerate playback but import a large surface and GPL obligations; it is not selected. Creating every suggested feature as a module now would inflate build/configuration overhead. Add modules when features need independent ownership.

## Module direction

`app` owns launch, navigation and Hilt composition. `core:model` owns immutable provider-independent music/social/playback types. `domain:music` owns `MusicSource`. `data:music` implements demo source and later isolated providers. `core:common` holds state/configuration types with no Android dependency. `core:database` owns Room cache entities/DAO/schema. `core:network` owns Ktor/Supabase creation and DTO boundaries. `core:designsystem` owns Compose theme/tokens/glass.

Dependencies flow UI -> repository/contract -> implementation -> infrastructure. Models have no Android, provider, Supabase or UI dependencies. Feature packages contain screen/ViewModel/state, not independent Track variants. Introduce repositories/use cases only for actual coordination or business rules; do not wrap one call in multiple abstractions.

## Shared models

Music identity is `(provider, providerId)`; serialize a namespaced canonical ID for internal use. Queue entries have separate occurrence IDs so replaying one track does not confuse queue edits. Track carries artists, optional album/artwork/duration, explicit flag and metadata. Album, Artist and Playlist share the identity scheme. `MusicEntity` distinguishes track/album/artist/playlist without feature-specific duplicates. Stream URLs are ephemeral playback data, never durable track identity or message content.

Social IDs are backend UUIDs. Message identity is a stable client-generated UUID, reconciled with server acknowledgement. Typed music attachments carry provider identity and a small metadata snapshot, not a trusted stream URL. ListeningActivity and Recommendation refer to the same music identity. PlaybackState is immutable and covers queue/index, status/buffering, position/duration, shuffle/repeat and structured errors.

## Music source plan

`MusicSource` supports search, getTrack/getAlbum/getArtist/getPlaylist, getStream, recommendations and related tracks. Keep provider JSON private to adapters. Use typed unavailable/not-found errors; preserve cancellation. DemoMusicSource supplies a small original synthetic catalog and deliberately has no commercial streams. Do not pass it off as a real provider.

Phase 2 selects an independently reviewed YouTube Music-compatible implementation after endpoint and dependency/license review. Encapsulate request policy, parsing, identifiers, URL expiry, throttling and failures. Resolve audio immediately before playback and refresh expired URLs with bounded retries. No DRM/protected-content bypass. Local and licensed adapters can later implement the same contract. Contract tests cover empty queries, malformed metadata, not found, timeout, expiry and cancellation.

## Playback plan

One ExoPlayer and MediaSession live inside PlaybackService (`MediaSessionService`), not an Activity or Composable. PlaybackController connects through a MediaController; PlayerRepository publishes StateFlow. ExoPlayer's timeline is authoritative while running. QueueManager performs deterministic edits through the same service; it does not maintain a competing active queue. Persist a restart snapshot, not a second live player.

Operations: play/pause/resume/seek/previous/next/shuffle/repeat/add/play-next/move/remove. All queue edits are serialized and validated against stable occurrence IDs. MediaSession commands from notification/headset/Bluetooth and in-app UI update the same state. Handle audio focus, noisy output, service/controller lifecycle, buffering, error recovery and stream expiration. Progress updates are local, collected only while needed; never send each tick to the server. Restore on explicit user action, never autoplay on launch.

App shell owns MiniPlayer outside tab routes. FullPlayer is an overlay route above the same state. Track playback from chat, search or activity invokes the same controller. Shared bounds and artwork identity give mini/full continuity. Palette extraction is cached by artwork identity and bounded; unavailable artwork uses accessible fallback colors.

## Database and offline plan

Room holds metadata, cached library, messages and durable outbox records when those features land. Phase 1 stores public catalog metadata only. Account-bound caches must be partitioned by account and purged on sign-out. DataStore holds nonsensitive UI preferences, not access tokens. Server mutations have Pending/Synced/Failed status and idempotency keys. Realtime is a hint followed by reconciliation; it is not the sole source of history.

## Messaging and sharing plan

One-to-one messaging first. A transactional backend RPC creates exactly two members after checking blocks and message permissions; clients cannot grant themselves arbitrary conversation membership. Sender identity comes from auth. Text, reply and music attachments use versioned typed payloads. Validate payload size/type and reply conversation on server.

Persist optimistic outbox entry -> send idempotently -> acknowledge canonical record -> reconcile Realtime. Order with `(created_at, id)` keyset cursors; reconnect fetches missed history, deduplicates IDs and ignores stale versions. Read receipts refer to an authorized conversation's server message watermark. Typing is ephemeral, throttled, expires automatically, and uses private authorized channels. Share cards resolve the canonical entity and invoke global playback; external sharing uses Android's chooser with stable links. HTTPS App Links require a controlled domain and assetlinks verification before release.

## Social and privacy plan

Follow and mutual relationships are directed server records. Blocking is bidirectional for eligibility and interactions. Backend visibility evaluates Everyone/Followers/Mutuals/Nobody separately for profile, presence, now-playing, history, activity and playlists. Defaults for listening/presence/history are Nobody; following never silently expands consent. Recommendations require a separate sharing opt-in.

Publish presence on state changes, with server time/position/status and expiry. Throttle seeks and heartbeat; clients estimate position and treat expired presence as absent. Changing privacy/blocking invalidates social feed caches and recommendation attribution. See DATABASE and RECOMMENDATIONS for backend enforcement; client hiding is not authorization.

## Recommendations, analytics and flags

RecommendationEngine owns configurable weighted ranking, penalties, diversity and deterministic exploration; repositories own candidates, behavioral events and TasteProfile. Introduce stable event IDs and no progress-tick storage. Analytics accepts allowlisted event fields, never text messages, auth tokens or raw personal content. Feature flags default off for shared listening/collaborative playlists/ML/experimental glass. No advanced ML before useful consented data exists.

## Environments and validation

Debug/staging/release use distinct application IDs and configuration. Unconfigured backend fails closed; the local catalog is explicitly local. Public URL/publishable key are safe only with correct RLS; reject known secret/service-role formats. Production credentials are never inferred from debug. No user/password token logging.

Run JVM tests for model/config/source behavior and later queue/ranking/taste. Run Android assemble and lint; Room and DataStore need device/instrumentation tests. Security requires real PostgreSQL/Supabase RLS adversarial tests. Screen and notification/device tests are release gates. A successful unit test does not prove background playback, accessibility, Realtime authorization or release readiness.
