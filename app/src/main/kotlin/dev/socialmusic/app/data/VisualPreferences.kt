package dev.socialmusic.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.socialmusic.common.VisualEffectLevel
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.displayPreferences by preferencesDataStore(name = "display")
private val effectsKey = stringPreferencesKey("effects")

@Singleton
class VisualPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    val effects = context.displayPreferences.data
        .catch { failure -> if (failure is IOException) emit(emptyPreferences()) else throw failure }
        .map { preferences ->
            VisualEffectLevel.entries.firstOrNull { it.name == preferences[effectsKey] } ?: VisualEffectLevel.FULL
        }
    suspend fun setEffects(level: VisualEffectLevel) {
        context.displayPreferences.edit { it[effectsKey] = level.name }
    }
}
