import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';
// Temporary fixture is generated locally and removed after verification.
const fixture = JSON.parse(readFileSync('.tools/auth-verification.json', 'utf8'));
const env = Object.fromEntries(readFileSync('.env', 'utf8').trim().split(/\r?\n/).map(line => line.split(/=(.*)/s).slice(0, 2)));
const url = env.ONDA_SUPABASE_URL;
const key = env.ONDA_SUPABASE_PUBLISHABLE_KEY;
async function post(path, body, token) {
  const response = await fetch(url + path, { method: 'POST', headers: {
    apikey: key, 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }, body: JSON.stringify(body), signal: AbortSignal.timeout(20_000) });
  const bodyText = await response.text();
  const value = bodyText ? JSON.parse(bodyText) : null;
  return { status: response.status, value };
}
const denied = await post('/functions/v1/recommendations', {});
assert.equal(denied.status, 401);
const invalid = await post('/functions/v1/recommendations', {}, 'invalid');
assert.ok([401, 403].includes(invalid.status));
const login = await post('/auth/v1/token?grant_type=password', { email: fixture.email, password: fixture.password });
assert.equal(login.status, 200, 'Password authentication failed');
const token = login.value.access_token;
assert.equal(login.value.user.id, fixture.id);
const rpc = (name, body = {}) => post(`/rest/v1/rpc/${name}`, body, token);
assert.equal((await rpc('setup_profile', { p_username: 'onda_verify_' + fixture.id.slice(0, 8), p_display_name: 'Verification' })).status, 204);
const home = await rpc('social_home');
assert.equal(home.status, 200);
assert.equal(home.value.settings.listening_shared, false);
assert.equal(home.value.settings.taste_shared, false);
assert.equal(home.value.settings.personalization_enabled, false);
assert.equal((await rpc('set_social_preferences', { p_listening_shared: false, p_taste_shared: false, p_private_session: false, p_personalization_enabled: true })).status, 204);
assert.equal((await rpc('record_listening_event', { p_id: '20000000-0000-0000-0000-000000000001', p_video_id: 'abcdefghijk', p_title: 'Verification song', p_artist: 'Verification artist', p_kind: 'like' })).status, 204);
const recommendations = await post('/functions/v1/recommendations', {}, token);
assert.equal(recommendations.status, 200);
assert.equal(recommendations.value.schema_version, 1);
assert.equal(recommendations.value.items[0].video_id, 'abcdefghijk');
assert.equal(recommendations.value.items[0].contributors.length, 0);
const refreshed = await post('/auth/v1/token?grant_type=refresh_token', { refresh_token: login.value.refresh_token });
assert.equal(refreshed.status, 200);
assert.equal((await post('/auth/v1/logout', {}, refreshed.value.access_token)).status, 204);
console.log('Live auth, private defaults, personal recommendations, refresh and logout passed; unauthenticated access rejected.');
