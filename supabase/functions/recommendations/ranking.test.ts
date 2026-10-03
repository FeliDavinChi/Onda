import { test } from 'node:test';
import assert from 'node:assert/strict';
import { rankCandidates } from './ranking.ts';

const candidate = (id: string, personal = 0.5) => ({ id, title: id, artist: 'Artist', videoId: id, personal });
const source = (enabled = false, consent = true, following = true, blocked = false) =>
  ({ id: 'friend', name: 'Maya', enabled, consent, following, blocked, affinities: { b: 1 } });

test('following alone does not introduce taste influence', () => {
  const result = rankCandidates([candidate('a', 0.55), candidate('b')], [source()], new Set());
  assert.equal(result[0].track.id, 'a');
  assert.equal(result[1].contributors.length, 0);
});
test('explicitly enabled permitted taste can influence ordering', () => {
  const result = rankCandidates([candidate('a', 0.55), candidate('b')], [source(true)], new Set());
  assert.equal(result[0].track.id, 'b');
  assert.deepEqual(result[0].contributors, [{ id: 'friend', name: 'Maya' }]);
});
test('blocks, unfollows and revoked consent remove imported influence', () => {
  for (const person of [source(true, false), source(true, true, false), source(true, true, true, true)]) {
    const result = rankCandidates([candidate('a', 0.55), candidate('b')], [person], new Set());
    assert.equal(result[0].track.id, 'a');
    assert.equal(result[1].contributors.length, 0);
  }
});
test('adding sources does not multiply the social budget', () => {
  const one = rankCandidates([candidate('b')], [source(true)], new Set())[0].score;
  const many = rankCandidates([candidate('b')], [source(true), { ...source(true), id: 'other' }], new Set())[0].score;
  assert.equal(one, many);
});
test('excluded tracks stay excluded and duplicate identities collapse', () => {
  const result = rankCandidates([candidate('a'), candidate('b'), candidate('b')], [source(true)], new Set(['a']));
  assert.deepEqual(result.map(item => item.track.id), ['b']);
});
test('sparse sources and nonfinite signals cannot invent affinity', () => {
  const result = rankCandidates([candidate('a', 0.55), candidate('b')],
    [{ ...source(true), affinities: { b: NaN } }], new Set());
  assert.equal(result[0].track.id, 'a');
  assert.equal(result[1].contributors.length, 0);
});
test('ranking does not mutate personal evidence or source signals', () => {
  const tracks = [candidate('b')]; const people = [source(true)];
  const before = JSON.stringify({ tracks, people });
  rankCandidates(tracks, people, new Set());
  assert.equal(JSON.stringify({ tracks, people }), before);
});
