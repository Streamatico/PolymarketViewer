package com.streamatico.polymarketviewer.data.preferences

import android.util.Log
import kotlinx.coroutines.flow.Flow

interface WatchlistInteractor {
    val watchlistIds: Flow<Set<String>>

    /**
     * Toggles event in watchlist.
     * Returns true when state was updated, false when watchlist limit is reached.
     */
    suspend fun toggleWatchlist(eventId: String): Boolean
}

class DefaultWatchlistInteractor(
    private val userPreferencesRepository: UserPreferencesRepository
) : WatchlistInteractor {

    override val watchlistIds: Flow<Set<String>> = userPreferencesRepository.watchlistIds

    override suspend fun toggleWatchlist(eventId: String): Boolean {
        val success = userPreferencesRepository.toggleWatchlist(eventId)
        if (!success) {
            // TODO: Emit a UI event (e.g. Snackbar) when watchlist limit is reached.
            Log.w(TAG, "Watchlist limit reached")
        }
        return success
    }
}

private const val TAG = "WatchlistInteractor"

