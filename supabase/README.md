# Onda Supabase

Provisioned project: akrsrxzfwbgjhpfmpoyc (Onda), FeliDavinChi's Org, ap-south-1/Mumbai, free plan. Dashboard: https://supabase.com/dashboard/project/akrsrxzfwbgjhpfmpoyc

Both migrations in migrations/ are deployed. The recommendations Edge Function is active and includes index.ts and ranking.ts. The local Android .env contains only the project URL and publishable key and is Git-ignored.

For another environment, create a Supabase project, apply the migrations in order and deploy recommendations with the configuration in config.toml. The function uses the built-in SUPABASE_URL and SUPABASE_ANON_KEY for downstream requests under the caller's JWT; ONDA_PUBLISHABLE_KEY can optionally replace the latter. No service-role key is used by this code. Missing/invalid/anonymous callers cannot execute recommendation_context.

Run tests/social_policy.sql against the database. It seeds three accounts within a transaction, impersonates authenticated roles, checks RLS and RPC behavior, and rolls everything back. Run ranking.test.ts using Node 24. scripts/verify-supabase.mjs checks live password auth, refresh/logout and the deployed function using a deliberately temporary confirmed test fixture at ignored .tools/auth-verification.json; fixture creation and deletion require administrative access, and the script itself does not grant privileges or disable project email confirmation.

Security advisor notices for authenticated SECURITY DEFINER RPCs are intentional: these narrowly scoped endpoints validate auth.uid(), fix an empty search_path, enforce permissions internally and revoke public/anonymous execution. This is the only remaining advisor category after the initial check. Reference: https://supabase.com/docs/guides/database/database-linter?lint=0029_authenticated_security_definer_function_executable

New profiles default to personalization, listening sharing and taste sharing on, with private session off. The `social_defaults` migration changes insertion defaults and preserves existing saved settings. Android waits for server settings before publishing. Run `tests/social_defaults.sql` to verify new-profile defaults and preservation of later choices, and `tests/social_policy.sql` for sharing and permission boundaries.

V1 scopes shared listening/taste to followers, collects event metadata only while the user's settings permit it, and uses a short control-write advisory lock for atomic revocation. Realtime, retention automation, scalable candidate retrieval and rate limiting need later production work. Configure SMTP and review Auth redirect/recovery settings before inviting a wider beta; the default hosted email service is limited.
