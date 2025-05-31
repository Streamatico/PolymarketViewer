package com.streamatico.polymarketviewer.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class UserPreferences( // Data class to hold related preferences
    val preferencesVersion: Int
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val prefsVersion = preferences[PreferenceKeys.PREFERENCES_VERSION] ?: 0
            UserPreferences(prefsVersion)
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
}

// Add migration logic if needed in the future
private const val LATEST_PREFERENCES_VERSION = 1