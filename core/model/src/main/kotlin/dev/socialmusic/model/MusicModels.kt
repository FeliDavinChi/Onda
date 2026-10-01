package dev.socialmusic.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistRef(val id: MusicId, val name: String)

@Serializable
data class AlbumRef(val id: MusicId, val title: String)

@Serializable
data class Track(
    val id: MusicId,
    val title: String,
    val artists: List<ArtistRef>,
    val album: AlbumRef? = null,
    val artwork: String? = null,
    val durationMs: Long? = null,
    val explicit: Boolean = false,
    val metadata: Map<String, String> = emptyMap(),
) {
    init {
        require(title.isNotBlank())
        require(durationMs == null || durationMs >= 0)
    }
}

data class Album(
    val id: MusicId,
    val title: String,
    val artists: List<ArtistRef>,
    val tracks: List<Track>,
    val artwork: String? = null,
)

data class Artist(
    val id: MusicId,
    val name: String,
    val albums: List<Album> = emptyList(),
    val topTracks: List<Track> = emptyList(),
    val artwork: String? = null,
)

data class Playlist(
    val id: MusicId,
    val title: String,
    val tracks: List<Track>,
    val artwork: String? = null,
)

sealed interface MusicEntity {
    val id: MusicId
    data class TrackEntity(val track: Track) : MusicEntity { override val id get() = track.id }
    data class AlbumEntity(val album: Album) : MusicEntity { override val id get() = album.id }
    data class ArtistEntity(val artist: Artist) : MusicEntity { override val id get() = artist.id }
    data class PlaylistEntity(val playlist: Playlist) : MusicEntity { override val id get() = playlist.id }
}

data class SearchResult(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val nextPageToken: String? = null,
)

data class StreamInfo(val url: String, val mimeType: String?, val expiresAtEpochMs: Long?)
data class RecommendationSeed(val trackIds: List<MusicId> = emptyList(), val artistIds: List<MusicId> = emptyList())
