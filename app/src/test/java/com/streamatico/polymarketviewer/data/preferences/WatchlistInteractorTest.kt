package com.streamatico.polymarketviewer.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.core.events.UiEvent
import com.streamatico.polymarketviewer.core.events.UiEventBus
import com.streamatico.polymarketviewer.core.events.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class WatchlistInteractorTest {

    @Test
    fun `emits snackbar event when watchlist limit is reached`() = runBlocking {
        val repository = createUserPreferencesRepository()
        repeat(MAX_WATCHLIST_SIZE) { index ->
            repository.toggleWatchlist("event-$index")
        }
        val eventBus = RecordingUiEventBus()
        val interactor = DefaultWatchlistInteractor(repository, eventBus)

        val success = interactor.toggleWatchlist("overflow-event")

        assertFalse(success)
        assertEquals(
            listOf(UiEvent.ShowSnackbar(UiText(R.string.watchlist_limit_reached, MAX_WATCHLIST_SIZE))),
            eventBus.recordedEvents
        )
    }

    @Test
    fun `does not emit snackbar when removing existing watchlist item`() = runBlocking {
        val repository = createUserPreferencesRepository()
        repeat(MAX_WATCHLIST_SIZE) { index ->
            repository.toggleWatchlist("event-$index")
        }
        val eventBus = RecordingUiEventBus()
        val interactor = DefaultWatchlistInteractor(repository, eventBus)

        val success = interactor.toggleWatchlist("event-0")

        assertTrue(success)
        assertTrue(eventBus.recordedEvents.isEmpty())
    }

    private fun createUserPreferencesRepository(): UserPreferencesRepository {
        return UserPreferencesRepository(createDataStore())
    }

    private fun createDataStore(): DataStore<Preferences> {
        val tempFile = Files.createTempFile("watchlist-interactor-test", ".preferences_pb")
            .toFile()
            .apply(File::deleteOnExit)

        return PreferenceDataStoreFactory.create(produceFile = { tempFile })
    }
}

private class RecordingUiEventBus : UiEventBus {
    val recordedEvents = mutableListOf<UiEvent>()

    override val events: Flow<UiEvent> = emptyFlow()

    override suspend fun emit(event: UiEvent) {
        recordedEvents += event
    }
}

