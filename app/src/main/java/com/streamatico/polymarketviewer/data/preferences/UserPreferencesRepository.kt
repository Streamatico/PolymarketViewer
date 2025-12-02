package com.streamatico.polymarketviewer.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map




data class UserPreferences( // Data class to hold related preferences
    val preferencesVersion: Int,
    val analyticsEnabled: Boolean = true, // Opt-out: enabled by default
    val isFirstLaunch: Boolean = true // True until first analytics ping is sent
)


class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val prefsVersion = preferences[PreferenceKeys.PREFERENCES_VERSION] ?: 0
            val analyticsEnabled = preferences[PreferenceKeys.ANALYTICS_ENABLED] ?: true
            val isFirstLaunch = preferences[PreferenceKeys.IS_FIRST_LAUNCH] ?: true
            UserPreferences(prefsVersion, analyticsEnabled, isFirstLaunch)
        }

    /**
     * Initializes the user preferences repository.
     */
    suspend fun initialize() {
        // Read current preferences once
        val preferencesVersion = userPreferencesFlow.first().preferencesVersion

        if (preferencesVersion == 0) {
            dataStore.edit {
                it[PreferenceKeys.PREFERENCES_VERSION] = LATEST_PREFERENCES_VERSION
            }
        }
    }

    /**
     * Sets whether analytics is enabled.
     */
    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ANALYTICS_ENABLED] = enabled
        }
    }

    /**
     * Marks that the first launch has been completed.
     */
    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_FIRST_LAUNCH] = false
        }
    }
}

// Add migration logic if needed in the future
private const val LATEST_PREFERENCES_VERSION = 1