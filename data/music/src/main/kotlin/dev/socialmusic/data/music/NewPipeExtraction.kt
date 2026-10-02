package dev.socialmusic.data.music

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.AudioStream
import java.io.IOException
import java.net.URI

internal class NewPipeExtraction : YoutubeExtraction {
    private fun service() = ServiceList.YouTube.also { initialize() }
    override fun search(query: String): List<YoutubeTrackData> {
        val extractor = service().getSearchExtractor(query, listOf("music_songs"), "")
        extractor.fetchPage()
        val page = extractor.initialPage
        if (page.items.isEmpty() && page.errors.isNotEmpty()) throw IOException("Music search parsing failed")
        return page.items.filterIsInstance<StreamInfoItem>().mapNotNull(::mapItem)
    }
    private fun stream(videoId: String) = service().getStreamExtractor("https://www.youtube.com/watch?v=$videoId").also { it.fetchPage() }
    override fun track(videoId: String): YoutubeTrackData = stream(videoId).let { YoutubeTrackData(videoId, it.name, it.uploaderName, channelId(it.uploaderUrl), it.thumbnails.maxByOrNull { image -> image.height }?.url, it.length) }
    override fun streams(videoId: String): List<YoutubeAudioData> = stream(videoId).audioStreams.map(::mapAudio)
    internal fun mapAudio(audio: AudioStream): YoutubeAudioData = YoutubeAudioData(
        audio.content, audio.format?.mimeType, audio.averageBitrate,
        audio.isUrl && audio.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP,
    )
    override fun related(videoId: String): List<YoutubeTrackData> = stream(videoId).relatedItems?.items?.filterIsInstance<StreamInfoItem>()?.mapNotNull(::mapItem).orEmpty()
    private fun mapItem(item: StreamInfoItem): YoutubeTrackData? = runCatching { YoutubeTrackData(service().streamLHFactory.getId(item.url), item.name, item.uploaderName, channelId(item.uploaderUrl), item.thumbnails.maxByOrNull { it.height }?.url, item.duration) }.getOrNull()
    private fun channelId(url: String?) = runCatching { URI(url).path.substringAfter("/channel/", "").takeIf { it.isNotBlank() && !it.contains('/') } }.getOrNull()
    companion object {
        @Volatile private var initialized = false
        private fun initialize() {
            if (!initialized) synchronized(this) { if (!initialized) { NewPipe.init(NewPipeDownloader()); initialized = true } }
        }
    }
}
