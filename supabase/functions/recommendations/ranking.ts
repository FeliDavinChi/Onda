// Onda's V1 ranker. Pure inputs/outputs let a future Python service implement
// the same HTTP contract without changing Android or social data ownership.
export type Candidate = {
  id: string; title: string; artist: string; videoId: string;
  artworkUrl?: string | null; personal: number;
};
export type TasteSource = {
  id: string; name: string; enabled: boolean; consent: boolean;
  following: boolean; blocked: boolean; affinities: Record<string, number>;
};
export type RankedTrack = { track: Candidate; score: number; contributors: { id: string; name: string }[] };
const bounded = (value: number) => Number.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;

export function rankCandidates(candidates: Candidate[], sources: TasteSource[], excluded: Set<string>): RankedTrack[] {
  const eligible = [...new Map(sources
    .filter(person => person.enabled && person.consent && person.following && !person.blocked)
    .map(person => [person.id, person] as const)).values()];
  const socialBudget = eligible.length ? 0.1 : 0;
  const unique = new Map<string, Candidate>();
  for (const track of candidates) {
    if (!track.id || excluded.has(track.id)) continue;
    const previous = unique.get(track.id);
    if (!previous || bounded(track.personal) > bounded(previous.personal)) unique.set(track.id, track);
  }
  return [...unique.values()].map(track => {
    const contributors = eligible.filter(person => bounded(person.affinities[track.id] ?? 0) > 0)
      .map(person => ({ id: person.id, name: person.name }));
    const social = eligible.reduce((sum, person) => sum + bounded(person.affinities[track.id] ?? 0), 0) / (eligible.length || 1);
    return { track, score: bounded(track.personal) * (1 - socialBudget) + social * socialBudget, contributors };
  }).sort((left, right) => right.score - left.score || (left.track.id < right.track.id ? -1 : left.track.id > right.track.id ? 1 : 0));
}
