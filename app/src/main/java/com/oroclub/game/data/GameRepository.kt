package com.oroclub.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = GameRepository.STORE_NAME
)

/**
 * The one and only persistence entry point of the app.
 *
 * Everything lives in a single DataStore Preferences key holding one serialized
 * JSON document. Reads are total: an empty store, a missing key, an empty string
 * or corrupted JSON all resolve to [GameData] defaults instead of crashing.
 */
class GameRepository(appContext: Context) {

    private val store: DataStore<Preferences> = appContext.applicationContext.progressDataStore

    val data: Flow<GameData> = store.data
        .catch {
            // An unreadable or corrupted store (IOException) — and anything else
            // the layer below can throw — resolves to defaults rather than
            // propagating into the UI.
            emit(emptyPreferences())
        }
        .map { prefs -> GameSerializer.decode(prefs[KEY_PAYLOAD]) }

    private suspend fun update(transform: (GameData) -> GameData) {
        try {
            store.edit { prefs ->
                val current = GameSerializer.decode(prefs[KEY_PAYLOAD])
                prefs[KEY_PAYLOAD] = GameSerializer.encode(transform(current))
            }
        } catch (io: IOException) {
            // Storage unavailable (full disk, locked profile). The session keeps
            // running with in-memory state; nothing is lost that matters.
        }
    }

    /** Stores [score] for [categoryId] only when it beats the stored best. */
    suspend fun saveQuizBest(categoryId: String, score: Int) {
        if (categoryId.isBlank()) return
        val clamped = score.coerceAtLeast(0)
        update { current ->
            val previous = current.quizBest[categoryId] ?: 0
            if (clamped <= previous) current
            else current.copy(quizBest = current.quizBest + (categoryId to clamped))
        }
    }

    /** Stores [matched] pairs for [level] only when it beats the stored best. */
    suspend fun savePairsBest(level: Int, matched: Int) {
        if (level <= 0) return
        val clamped = matched.coerceAtLeast(0)
        update { current ->
            val key = level.toString()
            val previous = current.pairsBest[key] ?: 0
            if (clamped <= previous) current
            else current.copy(pairsBest = current.pairsBest + (key to clamped))
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        update { current -> current.copy(settings = current.settings.copy(soundEnabled = enabled)) }
    }

    suspend fun setThemeMode(mode: String) {
        update { current -> current.copy(settings = current.settings.copy(themeMode = mode)) }
    }

    /** Wipes all progress and restores default settings. */
    suspend fun resetAll() {
        try {
            store.edit { prefs -> prefs[KEY_PAYLOAD] = GameSerializer.encode(GameData()) }
        } catch (io: IOException) {
            // Ignored on purpose: see update().
        }
    }

    companion object {
        const val STORE_NAME: String = "oro_club_progress"
        private val KEY_PAYLOAD = stringPreferencesKey("game_data_json")
    }
}
