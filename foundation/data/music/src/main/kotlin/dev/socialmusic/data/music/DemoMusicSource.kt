package dev.socialmusic.data.music

import dev.socialmusic.domain.music.*
import dev.socialmusic.model.*

/** Original synthetic metadata, with no audio streams or claims of provider availability. */
class DemoMusicSource : MusicSource {
    private val artist = ArtistRef(MusicId("demo", "artist-daylight"), "Daylight Assembly")
    private val album = AlbumRef(MusicId("demo", "album-open-windows"), "Open Windows")
    private val tracks = listOf(
        Track(MusicId("demo", "track-afterglow"), "Afterglow", listOf(artist), album, durationMs = 184000),
        Track(MusicId("demo", "track-paper-moons"), "Paper Moons", listOf(ArtistRef(MusicId("demo", "artist-soft-static"), "Soft Static")), durationMs = 215000),
        Track(MusicId("demo", "track-distant-lights"), "Distant Lights", listOf(ArtistRef(MusicId("demo", "artist-night-forms"), "Night Forms")), durationMs = 197000),
    )
    private val albums = listOf(Album(album.id, album.title, listOf(artist), tracks.take(1)))
    private val artists = tracks.flatMap { it.artists }.distinctBy { it.id }.map { ref ->
        Artist(ref.id, ref.name, albums.filter { item -> item.artists.any { it.id == ref.id } },
            tracks.filter { item -> item.artists.any { it.id == ref.id } })
    }
    private val playlists = listOf(Playlist(MusicId("demo", "playlist-first-light"), "First Light", tracks))

    override suspend fun search(query: String): SearchResult {
        val term = query.trim()
        if (term.isEmpty()) return SearchResult()
        return SearchResult(
            tracks = tracks.filter { it.title.contains(term, true) || it.artists.any { artist -> artist.name.contains(term, true) } },
            albums = albums.filter { it.title.contains(term, true) },
            artists = artists.filter { it.name.contains(term, true) },
            playlists = playlists.filter { it.title.contains(term, true) },
        )
    }
    override suspend fun getTrack(id: MusicId) = tracks.firstOrNull { it.id == id } ?: throw MusicSourceException.NotFound()
    override suspend fun getAlbum(id: MusicId) = albums.firstOrNull { it.id == id } ?: throw MusicSourceException.NotFound()
    override suspend fun getArtist(id: MusicId) = artists.firstOrNull { it.id == id } ?: throw MusicSourceException.NotFound()
    override suspend fun getPlaylist(id: MusicId) = playlists.firstOrNull { it.id == id } ?: throw MusicSourceException.NotFound()
    override suspend fun getStream(trackId: MusicId): StreamInfo {
        getTrack(trackId)
        throw MusicSourceException.StreamUnavailable()
    }
    override suspend fun getRecommendations(seed: RecommendationSeed) = tracks.toList()
    override suspend fun getRelatedTracks(trackId: MusicId): List<Track> {
        getTrack(trackId)
        return tracks.filter { it.id != trackId }
    }
}
