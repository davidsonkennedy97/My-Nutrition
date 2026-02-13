package com.example.nutriplan.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "nutriplan_settings"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

class LanguagePreferences(private val context: Context) {

    private val KEY_LANGUAGE = stringPreferencesKey("language_code")
    private val KEY_THEME = stringPreferencesKey("theme_mode") // "light" ou "dark"

    val languageCodeFlow: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_LANGUAGE] ?: "pt" }

    val themeModeFlow: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_THEME] ?: "light" }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { prefs -> prefs[KEY_LANGUAGE] = code }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME] = mode }
    }
}
