package dev.socialmusic.model

data class Recommendation(val track: Track, val reason: String, val configurationVersion: String)

data class TasteProfile(
    val userId: String,
    val artistWeights: Map<MusicId, Double> = emptyMap(),
    val genreWeights: Map<String, Double> = emptyMap(),
    val trackAffinity: Map<MusicId, Double> = emptyMap(),
    val albumAffinity: Map<MusicId, Double> = emptyMap(),
    val recentTaste: Map<String, Double> = emptyMap(),
    val longTermTaste: Map<String, Double> = emptyMap(),
    val discoveryPreference: Double = 0.1,
    val updatedAtEpochMs: Long,
)
