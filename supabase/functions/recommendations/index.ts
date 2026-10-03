import { rankCandidates } from './ranking.ts';
// Versioned contract is independent of where ranking is hosted.
Deno.serve(async (request: Request) => {
  if (request.method !== 'POST') return Response.json({ error: 'POST required' }, { status: 405 });
  const authorization = request.headers.get('Authorization');
  if (!authorization?.startsWith('Bearer ')) return Response.json({ error: 'Sign in required' }, { status: 401 });
  const url = Deno.env.get('SUPABASE_URL');
  const key = Deno.env.get('ONDA_PUBLISHABLE_KEY') ?? Deno.env.get('SUPABASE_ANON_KEY');
  if (!url || !key) return Response.json({ error: 'Recommendation service is not configured' }, { status: 503 });
  const headers = { apikey: key, Authorization: authorization, 'Content-Type': 'application/json' };
  try {
    // The database request uses the caller's JWT and auth.uid(), never an
    // Android-supplied user ID or a privileged service-role key.
    const contextRequest = () => fetch(`${url}/rest/v1/rpc/recommendation_context`, {
      method: 'POST', headers, body: '{}', signal: AbortSignal.timeout(10000),
    });
    const response = await contextRequest();
    if (!response.ok) return Response.json({ error: 'Recommendation access unavailable' }, { status: response.status === 401 ? 401 : 403 });
    const context = await response.json();
    const items = rankCandidates(context.candidates, context.sources, new Set(context.excluded)).slice(0, 30);
    // Recheck permissions after computation. If they changed, do not return
    // old friend candidates or explanations; ask the client to refresh.
    const recheck = await contextRequest();
    if (!recheck.ok) return Response.json({ error: 'Refresh recommendations' }, { status: 409 });
    const current = await recheck.json();
    if (JSON.stringify(current.sources.map((s: { id: string }) => s.id).sort()) !==
        JSON.stringify(context.sources.map((s: { id: string }) => s.id).sort()) || current.policy_version !== context.policy_version) {
      return Response.json({ error: 'Preferences changed; refresh recommendations' }, { status: 409 });
    }
    return Response.json({ schema_version: 1, config_version: 'taste-v1', policy_version: context.policy_version,
      generated_at: new Date().toISOString(), items: items.map(item => ({
        id: item.track.id, video_id: item.track.videoId, title: item.track.title, artist: item.track.artist,
        artwork_url: item.track.artworkUrl ?? null, contributors: item.contributors,
        reason: item.contributors.length ? `Inspired by ${item.contributors[0].name}'s music taste` : 'Based on your listening',
      })) }, { headers: { 'Cache-Control': 'no-store' } });
  } catch {
    return Response.json({ error: 'Recommendations unavailable; try again' }, { status: 503 });
  }
});
