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
import kotlinx.coroutines.flow.MutableStateFlow

import com.streamatico.polymarketviewer.ui.navigation.AppNavigation
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme



class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_EVENT_SLUG = "extra_event_slug"
    }

    private val startEventSlug = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startEventSlug.value = intent?.getStringExtra(EXTRA_EVENT_SLUG)

        setContent {
            PolymarketAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val initialEventSlug by startEventSlug.collectAsState()
                    // Setup Navigation
                    AppNavigation(initialEventSlug = initialEventSlug)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startEventSlug.value = intent.getStringExtra(EXTRA_EVENT_SLUG)
    }
}
