package dev.socialmusic.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.socialmusic.app.data.ListeningRecorder
import javax.inject.Inject

@HiltAndroidApp
class SocialMusicApplication : Application() {
    @Inject lateinit var listeningRecorder: ListeningRecorder
    override fun onCreate() { super.onCreate(); listeningRecorder.start() }
}
