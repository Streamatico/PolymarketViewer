package com.streamatico.polymarketviewer.data.preferences

import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.core.events.UiEvent
import com.streamatico.polymarketviewer.core.events.UiEventBus
import com.streamatico.polymarketviewer.core.events.UiText
import kotlinx.coroutines.flow.Flow

interface WatchlistInteractor {
    val watchlistIds: Flow<Set<String>>

    /**
     * Toggles event in watchlist.
     * Returns true when state was updated, false when watchlist limit is reached.
     *
     * When the limit is reached, implementations should also publish a user-visible
     * snackbar event explaining why the add operation was rejected.
     */
    suspend fun toggleWatchlist(eventId: String): Boolean
}

class DefaultWatchlistInteractor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val uiEventBus: UiEventBus
) : WatchlistInteractor {

    override val watchlistIds: Flow<Set<String>> = userPreferencesRepository.watchlistIds

    override suspend fun toggleWatchlist(eventId: String): Boolean {
        val success = userPreferencesRepository.toggleWatchlist(eventId)
        if (!success) {
            emitWatchlistLimitReachedMessage()
        }
        return success
    }

    private suspend fun emitWatchlistLimitReachedMessage() {
        uiEventBus.emit(
            UiEvent.ShowSnackbar(UiText(R.string.watchlist_limit_reached, MAX_WATCHLIST_SIZE))
        )
    }
}