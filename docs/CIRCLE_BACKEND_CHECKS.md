# Circle backend checks

Start with the smallest check that exercises the affected layer.

1. **Ranking only.** Run `node --experimental-strip-types --test supabase/functions/recommendations/ranking.test.ts` from the root with Node.js 24. This uses local fixtures.
2. **Database policy.** Use a database with the tracked [migrations](../supabase/migrations/) applied. [Social defaults](../supabase/tests/social_defaults.sql) checks inserted defaults and saved settings. [Social policies](../supabase/tests/social_policy.sql) exercises permission boundaries with transaction-scoped fixtures.
3. **Deployed authentication and recommendations.** For a test environment, the [live verification script](../scripts/verify-supabase.mjs) reads the Supabase URL and public key from root `.env` and a temporary confirmed test account from `.tools/auth-verification.json`. It expects that fixture to contain `id`, `email` and `password`. Run `node scripts/verify-supabase.mjs` from the root. The script writes profile and listening fixture data, so provision and remove that account through the test project's administrative workflow.
4. **Android behavior.** Follow the [two-account smoke test](CIRCLE_SMOKE_TEST.md) to check the actual app. Browser/API access alone cannot validate notification rendering, playback or in-app sign-in controls.

For Circle builds that report accounts unavailable, verify the app was compiled with the intended configuration. A live check returning 409 indicates a refresh is needed. See the [response reference](RECOMMENDATION_API.md) for the full list of codes.
