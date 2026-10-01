package dev.socialmusic.app.data

import dev.socialmusic.database.CachedTrack
import dev.socialmusic.database.MusicDatabase
import dev.socialmusic.domain.music.MusicSource
import dev.socialmusic.model.RecommendationSeed
import dev.socialmusic.model.Track
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

fun interface CatalogReader { suspend fun load(): List<Track> }

class CatalogRepository @Inject constructor(
    private val source: MusicSource,
    private val database: MusicDatabase,
) : CatalogReader {
    override suspend fun load(): List<Track> = withContext(Dispatchers.IO) {
        val tracks = source.getRecommendations(RecommendationSeed()).take(50)
        val timestamp = System.currentTimeMillis()
        database.cachedTracks().cache(tracks.map { track ->
            CachedTrack(
                track.id.canonical, track.id.provider, track.id.providerId,
                Json.encodeToString(Track.serializer(), track), timestamp,
            )
        })
        tracks
    }
}
