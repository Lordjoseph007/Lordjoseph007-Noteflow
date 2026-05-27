package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class FontSize { SMALL, MEDIUM, LARGE }

class SettingsManager(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        private val FONT_SIZE_KEY = stringPreferencesKey("font_size")
        private val APP_LOCK_ENABLED_KEY = booleanPreferencesKey("app_lock_enabled")
        private val APP_LOCK_PIN_KEY = stringPreferencesKey("app_lock_pin")
    }

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "Creator"
    }

    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val ordinal = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.ordinal
        ThemeMode.entries.getOrElse(ordinal) { ThemeMode.SYSTEM }
    }

    val fontSizeFlow: Flow<FontSize> = dataStore.data.map { preferences ->
        val value = preferences[FONT_SIZE_KEY] ?: FontSize.MEDIUM.name
        try {
            FontSize.valueOf(value)
        } catch (e: Exception) {
            FontSize.MEDIUM
        }
    }

    val appLockEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[APP_LOCK_ENABLED_KEY] ?: false
    }

    val appLockPinFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[APP_LOCK_PIN_KEY] ?: ""
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.ordinal
        }
    }

    suspend fun setFontSize(size: FontSize) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE_KEY] = size.name
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[APP_LOCK_ENABLED_KEY] = enabled
        }
    }

    suspend fun setAppLockPin(pin: String) {
        dataStore.edit { preferences ->
            preferences[APP_LOCK_PIN_KEY] = pin
        }
    }
}
