# Recommendations and taste profiles

Status: V1 backend ranking/taste design, not a running ranking service. Onda 0.4.0 implements separate private listening continuity: up to 20 unique recent songs on the device, with a clear action in Profile. The newest track identity can seed an upstream recommendation request on catalog refresh. There are no affinity scores, progress-tick events, cross-user signals or cloud history. The planned behavioral-event pipeline below requires consent/settings and backend isolation before collection.

## Ownership and pipeline

RecommendationSignalRepository stores idempotent meaningful events; TasteProfileRepository maintains private aggregates; RecommendationRepository assembles provider/history/social candidates; RecommendationEngine ranks and diversifies. Pipeline: authorized candidate generation -> deduplication/provider availability -> feature normalization -> negatives -> weighted ranking -> diversity/exploration -> recheck privacy -> concise explanation -> serve/cache with configuration version.

MusicSource returns provider-independent tracks; upstream scores are optional normalized hints. Never treat upstream rank as calibrated probability. Missing feature evidence is zero, not invented affinity. Cap candidate pools and page results.

## Central configuration

V1 starting ranking weights, sum 1: provider .30, artist .25, genre .15, history .15, social .10, exploration .05. Define one validated RecommendationWeights configuration with version and bounded values; reject negative/nonfinite weights and zero sums. Normalize similarity features to [0,1]. Experiments record config version and never scatter literals through code.

Starting behavior weights: significant listen +2; complete +3; replay +4; like +6; artist follow +8; playlist add +8; share +10; authorized direct share +5 contextual boost; early skip (before 15 seconds) -4; repeated skips -6; dislike -10. Avoid adding +2 and +3 for the same completed play unless intentionally configured: completion replaces significant-listen evidence for that play occurrence. PLAY_STARTED records exposure/context and contributes no affinity alone. Cap repeated event contributions to prevent spam. Stable event IDs ensure retries do not double count.

User can disable personalization, reset taste, exclude an artist and disable social contributions separately. Dislike/blocked artist/unavailable content are eligibility exclusions, not weak scoring nudges. Negative suppression always beats an upstream score.

## Taste profile plan

Private per-user profile: artistWeights, genreWeights, trackAffinity, albumAffinity, longTermTaste, recentTaste, discoveryPreference, updatedAt, schema/config version. Preserve raw sparse evidence, derive bounded scores for ranking. Affinity updates use metadata attached to an event; missing genres are unknown. Do not invent genre tags from names.

Decay factor `2^(-age / halfLife)` with injected clock. Initial half-life 90 days long-term and 6 hours session, centrally configured. Clamp negative age from device skew to zero; trusted receive time bounds client timestamps. Combine session/long-term features at .35/.65 initially. Recent skips should suppress session candidates promptly; likes/follows influence durable taste. Onboarding is low-confidence evidence that loses relative influence as behavior grows. Never expose numeric taste scores as scientific certainty.

## Candidate ranking and penalties

Weighted sum uses normalized features, then subtract centrally configured recent-skip/repetition/exposure penalties. Hard-filter dislikes, blocked artists, unavailable streams and unauthorized social evidence before scoring. Reject nonfinite data. Canonical IDs deduplicate across candidate sources; multi-provider content does not assume two IDs mean the same recording without verified metadata matching.

History features favor genuine engagement, not counts of player ticks. Session candidates reflect the current listening context; recommendation clicks alone are weaker than completion/save/share. Exploration feature affects scoring, but an explicit quota reserves unfamiliar candidates so they are not always buried.

## Diversity and exploration

Greedy rerank avoids adjacent identical primary artists where an alternative exists; cap two primary-artist tracks per ten-item window and repeated albums. Relax caps only when the eligible pool cannot fill, record the relaxation, and keep hard privacy/dislike exclusions. Artist-radio intent may deliberately relax artist diversity. Artist sets, not only the first credited artist, inform similarity where metadata supports it.

Reserve 5-10% exploration slots; initial 10% on ten-item pages. Use a carry-forward quota for smaller pages instead of rounding every page to zero. Select from adjacent genres/related or emerging artists/new releases and consented friend discoveries. Inject a deterministic exploration selector and stable tie-breaking by canonical ID; do not use uncontrolled randomness in tests. Cold start uses onboarding/provider diversity with honest reasons, not nonexistent personal history.

## Social scoring and privacy

Compute weighted artist/track/genre overlap only from eligible shared evidence. Increase contextual weight for mutual relationships and authorized direct shares. Normalize to avoid high-volume listeners dominating. Broad follow activity alone is not strong taste compatibility. No raw chat text enters ranking; only an authorized typed music attachment can create a contextual direct-share event.

Cross-user recommendation use requires separate consent plus surface visibility and block checks. A public profile does not imply public listening history or reusable taste. Private taste vectors remain owner/backend-only; a trusted backend may derive permitted shared aggregates without revealing private vectors. Do not compare private vectors to create an exposed compatibility score without mutual eligibility/consent. The receiver's own response to a shared song can update their taste independently of the sender's private history.

Recheck authorization when serving and explaining, including cached results. On block/privacy change, invalidate contributor mappings, explanations and eligible social candidates. Do not emit "because [person] listened" without current permission, or imply anonymization alone makes unauthorized private data usable. If consent disappears, remove the social contribution; the track can remain only if independently supported and explained by eligible nonsocial evidence.

## Explanations and Home

Select the strongest truthful authorized reason: artist affinity, new release, related playlist, recent direct share, or eligible friend listening. Labels stay lightweight. Home selects a few useful sections based on available evidence rather than displaying every category. Raw scores remain internal debug data scoped to the owner/trusted staff, never normal UX.

## Deterministic acceptance tests

- Like increases related artist affinity; early skip decreases it; replay increases it.
- Older identical evidence contributes less than recent evidence; session taste differs from durable taste.
- Duplicate event ID does not increment twice; same track different play occurrences remain distinct.
- Completed occurrence does not double-count its significant-listen threshold.
- Explicit dislike excludes a candidate despite maximum upstream score.
- Artist/album diversity avoids unnecessary repeats; insufficient pools relax documented soft caps only.
- Exploration appears at configured quotas across small pages and never bypasses hard filters.
- Blocked/private/nonconsenting friends contribute zero, including stale cached attribution.
- Direct music shares affect ranking only for authorized recipients; text messages never enter signals.
- All-zero/missing signals return diverse cold-start results or a proper empty state; deterministic tie order.
- Invalid config, future timestamps, malformed/nonfinite features and unavailable tracks are handled explicitly.

## Evolution

Evaluate completion, skips, saves, hides and diversity with consented aggregate metrics. Establish a V1 baseline and bias/exposure checks before collaborative filtering. Later add embeddings/ANN or contextual ranking behind the same candidate/features interfaces and versioned flags. Shared mixes require mutual privacy eligibility and balance both users; they are deferred along with public compatibility claims. No sensitive location/device/activity collection is required for V1.
