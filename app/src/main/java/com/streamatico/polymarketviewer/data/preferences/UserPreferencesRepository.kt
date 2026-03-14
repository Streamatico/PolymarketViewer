package com.streamatico.polymarketviewer.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences( // Data class to hold related preferences
    val analyticsEnabled: Boolean = true, // Opt-out: enabled by default
    val isFirstLaunch: Boolean = true, // True until first analytics ping is sent
    val watchlistIds: Set<String> = emptySet(),
    val isWatchlistSelected: Boolean = false
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                analyticsEnabled = preferences[PreferenceKeys.ANALYTICS_ENABLED] ?: true,
                isFirstLaunch = preferences[PreferenceKeys.IS_FIRST_LAUNCH] ?: true,
                watchlistIds = preferences[PreferenceKeys.WATCHLIST_EVENT_IDS] ?: emptySet(),
                isWatchlistSelected = preferences[PreferenceKeys.IS_WATCHLIST_SELECTED] ?: false
            )
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

    /**
     * Toggles a specific event ID in the watchlist.
     * Returns true if the operation was successful, false if the limit was reached.
     */
    suspend fun toggleWatchlist(eventId: String): Boolean {
        var success = true
        dataStore.edit { preferences ->
            val currentWatchlist = preferences[PreferenceKeys.WATCHLIST_EVENT_IDS] ?: emptySet()
            if (currentWatchlist.contains(eventId)) {
                preferences[PreferenceKeys.WATCHLIST_EVENT_IDS] = currentWatchlist - eventId
            } else {
                if (currentWatchlist.size >= MAX_WATCHLIST_SIZE) {
                    success = false
                } else {
                    preferences[PreferenceKeys.WATCHLIST_EVENT_IDS] = currentWatchlist + eventId
                }
            }
        }
        return success
    }

    val watchlistIds: Flow<Set<String>> = userPreferencesFlow.map { it.watchlistIds }

    val isWatchlistSelected: Flow<Boolean> = userPreferencesFlow.map { it.isWatchlistSelected }

    suspend fun setWatchlistSelected(isSelected: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IS_WATCHLIST_SELECTED] = isSelected
        }
    }
}

private const val MAX_WATCHLIST_SIZE = 50