package com.streamatico.polymarketviewer.ui.shared.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.core.events.DefaultUiEventBus
import com.streamatico.polymarketviewer.core.events.UiEvent
import com.streamatico.polymarketviewer.core.events.UiText
import com.streamatico.polymarketviewer.data.preferences.MAX_WATCHLIST_SIZE
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GlobalSnackbarHostTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun latestSnackbarReplacesCurrentOne() {
        val eventBus = DefaultUiEventBus()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val firstMessage = context.getString(R.string.app_name)
        val secondMessage = context.getString(R.string.watchlist_limit_reached, MAX_WATCHLIST_SIZE)

        composeTestRule.setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    GlobalSnackbarHost(
                        uiEvents = eventBus.events,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        runBlocking {
            eventBus.emit(UiEvent.ShowSnackbar(UiText(R.string.app_name)))
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(firstMessage).fetchSemanticsNodes().isNotEmpty()
        }
        assertTrue(
            composeTestRule.onAllNodesWithText(firstMessage).fetchSemanticsNodes().isNotEmpty()
        )

        runBlocking {
            eventBus.emit(
                UiEvent.ShowSnackbar(
                    UiText(R.string.watchlist_limit_reached, MAX_WATCHLIST_SIZE)
                )
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(secondMessage).fetchSemanticsNodes().isNotEmpty() &&
                composeTestRule.onAllNodesWithText(firstMessage).fetchSemanticsNodes().isEmpty()
        }

        assertTrue(
            composeTestRule.onAllNodesWithText(secondMessage).fetchSemanticsNodes().isNotEmpty()
        )
        assertTrue(
            composeTestRule.onAllNodesWithText(firstMessage).fetchSemanticsNodes().isEmpty()
        )
    }
}


