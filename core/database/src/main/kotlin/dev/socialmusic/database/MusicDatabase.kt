package dev.socialmusic.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedTrack::class], version = 1, exportSchema = true)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun cachedTracks(): CachedTrackDao
}
