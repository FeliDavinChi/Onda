# Android foundation design

The user supplied a complete product specification and explicitly asked to make reasonable engineering choices, create planning documents, then begin Phase 1. After reconnaissance revealed a different product, the user directed a new repository. The approved scope is an independent native Android foundation; no conversion of VoiceNotes.

Use a small modular Kotlin repository. Domain music IDs and models are shared across every feature. Implement a replaceable source contract and an original deterministic metadata-only demo source. Keep backend credentials optional and fail closed, with public keys only. Establish Room public-metadata caching, Hilt bindings, DataStore visual-effect preference and a lifecycle-aware Compose shell. Adopt glass through centralized tint/border/elevation tokens; true backdrop blur awaits measured implementation.

The shell has Home, Explore, Library, Messages and Profile destinations. Home displays original demo metadata; deferred destinations give honest empty states. Profile controls actual persisted effects. No fake player, chat, auth or remote mutation success. Production playback/provider implementation starts in Phase 2 and must pass device stability gates before further feature work.

Tests cover identity collision/encoding/validation, backend configuration, deterministic source search and unavailable streams. Validate Android compilation and lint. Device UI, Room migration, accessibility and server RLS tests are explicitly separate evidence. The five root system documents and ROADMAP carry future system details.

Alternatives rejected: repurposing VoiceNotes (wrong architecture/product), importing the entire GPL reference (scope/license implications), and prematurely scaffolding all proposed feature modules (maintenance overhead). Brand/app ID are provisional development identifiers; final production identity and code distribution license require a separate release decision.

See ARCHITECTURE, DATABASE, DESIGN_SYSTEM, RECOMMENDATIONS, ROADMAP and RECONNAISSANCE for contracts and acceptance gates.
