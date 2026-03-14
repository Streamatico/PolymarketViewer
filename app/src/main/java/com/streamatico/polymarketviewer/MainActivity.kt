package com.streamatico.polymarketviewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

import com.streamatico.polymarketviewer.ui.navigation.AppNavigation
import com.streamatico.polymarketviewer.ui.navigation.WidgetOpenCoordinator
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme



class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_EVENT_SLUG = "extra_event_slug"
    }

    private val widgetOpenCoordinator = WidgetOpenCoordinator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        widgetOpenCoordinator.onWidgetIntentSlug(intent?.getStringExtra(EXTRA_EVENT_SLUG))

        setContent {
            PolymarketAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentOpenEventCommand by widgetOpenCoordinator.openEventCommand.collectAsState()

                    // Recreate navigation graph for widget-open commands so the target event
                    // is the very first rendered screen, without briefly showing stale content.
                    key(currentOpenEventCommand?.requestId ?: Long.MIN_VALUE) {
                        AppNavigation(
                            initialEventSlug = currentOpenEventCommand?.eventSlug,
                            onTopEventSlugChanged = widgetOpenCoordinator::onTopEventSlugChanged
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetOpenCoordinator.onWidgetIntentSlug(intent.getStringExtra(EXTRA_EVENT_SLUG))
    }
}
