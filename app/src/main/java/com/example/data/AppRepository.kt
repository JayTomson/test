package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "readtracker_prefs")

class AppRepository(private val context: Context) {

    private object Keys {
        val BOOKS_JSON = stringPreferencesKey("books_json")
        val BOOKS_BACKUP = stringPreferencesKey("books_backup")
        val SETTINGS_JSON = stringPreferencesKey("settings_json")
    }

    val booksFlow: Flow<Pair<List<Book>, Boolean>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val rawJson = prefs[Keys.BOOKS_JSON] ?: ""
            Book.parseLibrary(rawJson)
        }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val rawJson = prefs[Keys.SETTINGS_JSON] ?: ""
            AppSettings.parseSettings(rawJson)
        }

    suspend fun saveBooks(books: List<Book>) {
        withContext(Dispatchers.Default) {
            val jsonStr = Book.encodeLibrary(books)
            context.dataStore.edit { prefs ->
                prefs[Keys.BOOKS_JSON] = jsonStr
            }
        }
    }

    suspend fun saveBooksCorruptedBackup(rawString: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BOOKS_BACKUP] = rawString
        }
    }

    suspend fun saveSettings(settings: AppSettings) {
        withContext(Dispatchers.Default) {
            val jsonStr = AppSettings.encodeSettings(settings)
            context.dataStore.edit { prefs ->
                prefs[Keys.SETTINGS_JSON] = jsonStr
            }
        }
    }
}
