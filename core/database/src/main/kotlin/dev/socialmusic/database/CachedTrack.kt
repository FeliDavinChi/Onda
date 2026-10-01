package dev.socialmusic.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_tracks",
    indices = [Index(value = ["provider", "providerId"], unique = true), Index("cachedAtEpochMs")],
)
data class CachedTrack(
    @PrimaryKey val id: String,
    val provider: String,
    val providerId: String,
    val payloadJson: String,
    val cachedAtEpochMs: Long,
)
