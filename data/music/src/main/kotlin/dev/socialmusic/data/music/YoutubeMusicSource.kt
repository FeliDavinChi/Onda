package dev.socialmusic.data.music

import dev.socialmusic.domain.music.MusicSource
import dev.socialmusic.domain.music.MusicSourceException
import dev.socialmusic.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.net.URI

internal data class YoutubeTrackData(val videoId: String, val title: String, val artist: String? = null, val channelId: String? = null, val artwork: String? = null, val durationSeconds: Long? = null)
internal data class YoutubeAudioData(val url: String, val mimeType: String?, val bitrate: Int, val progressive: Boolean = true)
internal interface YoutubeExtraction {
    fun search(query: String): List<YoutubeTrackData>
    fun track(videoId: String): YoutubeTrackData
    fun streams(videoId: String): List<YoutubeAudioData>
    fun related(videoId: String): List<YoutubeTrackData>
}

class YoutubeMusicSource internal constructor(private val extraction: YoutubeExtraction, private val nowMs: () -> Long = System::currentTimeMillis) : MusicSource {
    constructor() : this(NewPipeExtraction())
    override suspend fun search(query: String): SearchResult {
        val trimmed = query.trim().take(500)
        if (trimmed.isEmpty()) return SearchResult()
        return extract { SearchResult(tracks = mapTracks(extraction.search(trimmed))) }
    }
    override suspend fun getTrack(id: MusicId): Track {
        validate(id)
        return extract { mapTrack(extraction.track(id.providerId))?.takeIf { it.id == id } ?: throw MusicSourceException.NotFound() }
    }
    override suspend fun getStream(id: MusicId): StreamInfo {
        validate(id)
        return extract {
            extraction.streams(id.providerId).sortedByDescending { it.bitrate }.firstNotNullOfOrNull { audio ->
                if (!audio.progressive || audio.mimeType !in setOf("audio/mp4", "audio/webm", "audio/mpeg", "audio/ogg")) return@firstNotNullOfOrNull null
                val uri = secureUri(audio.url) ?: return@firstNotNullOfOrNull null
                val expiryText = uri.rawQuery?.split('&')?.firstOrNull { it.substringBefore('=') == "expire" }?.substringAfter('=')
                val expiry = if (expiryText != null) expiryText.toLongOrNull()?.takeIf { it > 0 && it <= Long.MAX_VALUE / 1000 }?.times(1000) ?: return@firstNotNullOfOrNull null else null
                if (expiry != null && expiry <= nowMs()) return@firstNotNullOfOrNull null
                StreamInfo(audio.url, audio.mimeType, expiry)
            } ?: throw MusicSourceException.StreamUnavailable()
        }
    }
    override suspend fun getRelatedTracks(id: MusicId): List<Track> {
        validate(id)
        return extract { mapTracks(extraction.related(id.providerId)).filter { it.id != id } }
    }
    override suspend fun getRecommendations(seed: RecommendationSeed): List<Track> = seed.trackIds.firstOrNull()?.let { getRelatedTracks(it) } ?: search("new music").tracks
    override suspend fun getAlbum(id: MusicId): Album = throw MusicSourceException.NotFound()
    override suspend fun getArtist(id: MusicId): Artist = throw MusicSourceException.NotFound()
    override suspend fun getPlaylist(id: MusicId): Playlist = throw MusicSourceException.NotFound()
    private fun validate(id: MusicId) { if (id.provider != "youtube" || !videoId.matches(id.providerId)) throw MusicSourceException.NotFound() }
    private fun mapTracks(rows: List<YoutubeTrackData>) = rows.mapNotNull(::mapTrack).distinctBy { it.id }.take(50)
    private fun mapTrack(row: YoutubeTrackData): Track? {
        if (!videoId.matches(row.videoId)) return null
        val artist = row.artist?.trim()?.takeIf { it.isNotEmpty() }
        val channel = row.channelId?.takeIf { it.isNotBlank() && it.length < 480 }
        return Track(MusicId("youtube", row.videoId), row.title.trim().ifEmpty { "Untitled track" },
            if (artist != null && channel != null) listOf(ArtistRef(MusicId("youtube", "channel:$channel"), artist)) else emptyList(),
            artwork = row.artwork?.takeIf { secureUri(it) != null },
            durationMs = row.durationSeconds?.takeIf { it >= 0 && it <= Long.MAX_VALUE / 1000 }?.times(1000))
    }
    private suspend fun <T> extract(block: () -> T): T = try { runInterruptible(Dispatchers.IO, block) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (typed: MusicSourceException) { throw typed }
        catch (_: Exception) { throw MusicSourceException.ProviderUnavailable() }
    companion object {
        private val videoId = Regex("[a-zA-Z0-9_-]{11}")
        private fun secureUri(url: String): URI? = runCatching { URI(url).takeIf { it.scheme == "https" && !it.host.isNullOrBlank() && it.userInfo == null } }.getOrNull()
    }
}
