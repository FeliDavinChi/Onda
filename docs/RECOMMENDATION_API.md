# Circle recommendations API

Clients send `POST /functions/v1/recommendations` with the signed-in user's bearer token. The [handler](../supabase/functions/recommendations/index.ts) requests a permission-filtered context from Supabase using that token and checks the context again after ranking.

A successful response includes `schema_version`, `config_version`, `policy_version`, `generated_at` and up to 30 items. Each item carries its canonical `id`, `video_id`, `title`, `artist`, nullable `artwork_url`, `contributors` and a user-facing `reason`. The response uses `Cache-Control: no-store`.

| Status | How to interpret it |
| --- | --- |
| 401 | Sign-in is required or the current token was rejected |
| 403 | The context request could not access recommendation data |
| 405 | The endpoint expects a POST request |
| 409 | Preferences or source eligibility may have changed during ranking. Refresh the recommendations with current account settings |
| 503 | The service is unconfigured or temporarily unavailable |

The [ranking tests](../supabase/functions/recommendations/ranking.test.ts) exercise scoring, source eligibility and the shared influence budget. The [backend guide](../supabase/README.md#verify) covers policy and live service checks.
