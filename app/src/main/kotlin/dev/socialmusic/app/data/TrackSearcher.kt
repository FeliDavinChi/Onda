package dev.socialmusic.app.data

import dev.socialmusic.model.Track

fun interface TrackSearcher { suspend fun search(query: String): List<Track> }
