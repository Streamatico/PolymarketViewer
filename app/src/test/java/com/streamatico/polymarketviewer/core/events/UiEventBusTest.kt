package com.streamatico.polymarketviewer.core.events

import com.streamatico.polymarketviewer.R
import kotlinx.coroutines.async
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UiEventBusTest {

    @Test
    fun `emits snackbar event to collectors`() = runBlocking {
        val bus = DefaultUiEventBus()
        val expected = UiEvent.ShowSnackbar(UiText(R.string.app_name))

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            withTimeout(1_000) {
                bus.events.first()
            }
        }

        bus.emit(expected)

        assertEquals(expected, deferred.await())
    }
}




