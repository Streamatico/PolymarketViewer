package com.streamatico.polymarketviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.data.analytics.AnalyticsEvent
import com.streamatico.polymarketviewer.data.analytics.AnalyticsService
import com.streamatico.polymarketviewer.core.events.UiEventBus
import com.streamatico.polymarketviewer.ui.navigation.AppNavigation
import com.streamatico.polymarketviewer.ui.navigation.WidgetOpenCoordinator
import com.streamatico.polymarketviewer.ui.shared.components.GlobalSnackbarHost
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val analyticsService: AnalyticsService by inject()
    private val uiEventBus: UiEventBus by inject()

    companion object {
        const val EXTRA_EVENT_SLUG = "extra_event_slug"
        private var firstLaunchTracked = false
    }

    private val widgetOpenCoordinator = WidgetOpenCoordinator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        trackAppLaunched()

        widgetOpenCoordinator.onWidgetIntentSlug(intent?.getStringExtra(EXTRA_EVENT_SLUG))

        setContent {
            PolymarketAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRootContent(
                        uiEventBus = uiEventBus,
                        widgetOpenCoordinator = widgetOpenCoordinator
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetOpenCoordinator.onWidgetIntentSlug(intent.getStringExtra(EXTRA_EVENT_SLUG))
    }

    private fun trackAppLaunched() {
        // Track only once per app launch
        if (!firstLaunchTracked) {
            firstLaunchTracked = true
            analyticsService.track(AnalyticsEvent.AppLaunched)
        }
    }
}

@Composable
private fun AppRootContent(
    uiEventBus: UiEventBus,
    widgetOpenCoordinator: WidgetOpenCoordinator
) {
    val currentOpenEventCommand by widgetOpenCoordinator.openEventCommand.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Recreate navigation graph for widget-open commands so the target event
        // is the very first rendered screen, without briefly showing stale content.
        key(currentOpenEventCommand?.requestId ?: Long.MIN_VALUE) {
            AppNavigation(
                initialEventSlug = currentOpenEventCommand?.eventSlug,
                onTopEventSlugChanged = widgetOpenCoordinator::onTopEventSlugChanged
            )
        }

        GlobalSnackbarHost(
            uiEvents = uiEventBus.events,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

