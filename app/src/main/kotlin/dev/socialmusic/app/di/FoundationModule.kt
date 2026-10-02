package dev.socialmusic.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.socialmusic.app.data.CatalogReader
import dev.socialmusic.app.data.CatalogRepository
import dev.socialmusic.data.music.YoutubeMusicSource
import dev.socialmusic.app.data.TrackSearcher
import dev.socialmusic.core.playback.SessionPlaybackController
import dev.socialmusic.domain.music.PlaybackController
import dev.socialmusic.database.MusicDatabase
import dev.socialmusic.domain.music.MusicSource
import dev.socialmusic.network.BackendClientFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FoundationModule {
    @Provides @Singleton fun musicSource(): MusicSource = YoutubeMusicSource()
    @Provides @Singleton fun searcher(source: MusicSource): TrackSearcher = TrackSearcher { source.search(it).tracks }
    @Provides @Singleton fun playback(@ApplicationContext context: Context): PlaybackController = SessionPlaybackController(context)
    @Provides @Singleton fun database(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, "public-catalog.db").build()
    @Provides @Singleton fun catalog(repository: CatalogRepository): CatalogReader = repository
    @Provides @Singleton fun backendFactory(): BackendClientFactory = BackendClientFactory()
}
