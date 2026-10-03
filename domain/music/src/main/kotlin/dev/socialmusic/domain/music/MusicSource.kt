package dev.socialmusic.domain.music

import dev.socialmusic.model.*

interface MusicSource {
    suspend fun search(query: String): SearchResult
    suspend fun getTrack(id: MusicId): Track
    suspend fun getAlbum(id: MusicId): Album
    suspend fun getArtist(id: MusicId): Artist
    suspend fun getPlaylist(id: MusicId): Playlist
    suspend fun getStream(trackId: MusicId): StreamInfo
    suspend fun getRecommendations(seed: RecommendationSeed): List<Track>
    suspend fun getRelatedTracks(trackId: MusicId): List<Track>
}

sealed class MusicSourceException(message: String) : Exception(message) {
    class NotFound : MusicSourceException("Music entity not found")
    class StreamUnavailable : MusicSourceException("Audio unavailable for this source")
    class ProviderUnavailable : MusicSourceException("Music provider unavailable. Please try again.")
}
