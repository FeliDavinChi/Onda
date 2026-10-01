package dev.socialmusic.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CachedTrackDao {
    @Query("SELECT * FROM cached_tracks ORDER BY cachedAtEpochMs DESC, id ASC LIMIT 50")
    abstract fun observeRecent(): Flow<List<CachedTrack>>

    @Upsert
    protected abstract suspend fun upsert(rows: List<CachedTrack>)

    @Query("DELETE FROM cached_tracks WHERE id NOT IN (SELECT id FROM cached_tracks ORDER BY cachedAtEpochMs DESC, id ASC LIMIT 200)")
    protected abstract suspend fun prune()

    @Transaction
    open suspend fun cache(rows: List<CachedTrack>) {
        require(rows.size <= 200) { "Catalog cache batch exceeds limit" }
        upsert(rows)
        prune()
    }
}
