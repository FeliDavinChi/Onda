# Onda Circle backend

Supabase provides Onda's account, following, listening activity, privacy controls and recommendation data. The deployed preview project is in Mumbai (`ap-south-1`) on the free plan. [Onda dashboard](https://supabase.com/dashboard/project/akrsrxzfwbgjhpfmpoyc)

## Deploy

1. Create or select a Supabase project.
2. Apply the files in [`migrations/`](migrations/) in filename order.
3. Deploy the [recommendations Edge Function](functions/recommendations/) using [`config.toml`](config.toml).
4. Configure Android with the project URL and public publishable key as described in the [Onda README](../README.md).

The function uses Supabase's built-in `SUPABASE_URL` and `SUPABASE_ANON_KEY` for downstream requests under the caller's JWT. An environment can provide `ONDA_PUBLISHABLE_KEY` to use in place of the anonymous key. Authentication is enforced in the handler and RPCs. Missing, invalid or anonymous callers cannot execute `recommendation_context`.

## Verify

| Check | What it covers |
| --- | --- |
| [`tests/social_policy.sql`](tests/social_policy.sql) | Follower visibility, RLS and RPC permissions using transaction-scoped test accounts |
| [`tests/social_defaults.sql`](tests/social_defaults.sql) | New-profile defaults and preservation of later settings changes |
| [`ranking.test.ts`](functions/recommendations/ranking.test.ts) | Recommendation ranking in Node.js 24 |
| [`verify-supabase.mjs`](../scripts/verify-supabase.mjs) | Live password sign-in, refresh, sign-out and the deployed function |

Run the SQL files against the configured database. The live script uses a temporary confirmed test account saved in the ignored `.tools/auth-verification.json`. Fixture creation and removal require administrative access.

From the repository root:

```bash
node --experimental-strip-types --test supabase/functions/recommendations/ranking.test.ts
```

## Privacy and preview operations

- New profiles start with personalization, listening sharing and taste sharing enabled, and private session off. The defaults migration preserves saved settings. Android loads the server settings before publishing.
- Shared listening and taste are scoped to followers. The backend collects event metadata only when settings allow it and uses a short advisory lock to make revocation atomic.
- Authenticated `SECURITY DEFINER` RPCs validate `auth.uid()`, use an empty `search_path`, check permissions internally and revoke public and anonymous execution. [Supabase advisor reference](https://supabase.com/docs/guides/database/database-linter?lint=0029_authenticated_security_definer_function_executable)
- Configure SMTP and review Auth redirect and recovery settings before a wider beta. Realtime, retention automation, scalable candidate retrieval and rate limiting are future work.
