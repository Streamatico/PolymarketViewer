package com.streamatico.polymarketviewer.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.streamatico.polymarketviewer.data.analytics.AnalyticsEvent
import com.streamatico.polymarketviewer.data.analytics.AnalyticsService
import com.streamatico.polymarketviewer.ui.event_list.SelectEventForWidgetScreen
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class EventWidgetConfigActivity : ComponentActivity() {
    private val appWidgetId: Int by lazy {
        intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val analyticsService: AnalyticsService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            PolymarketAppTheme {
                SelectEventForWidgetScreen(
                    onNavigateBack = { finish() },
                    onEventSelected = { event ->
                        lifecycleScope.launch {
                            EventWidgetUpdater.saveSelection(
                                context = this@EventWidgetConfigActivity,
                                appWidgetId = appWidgetId,
                                selection = EventWidgetSelection(
                                    eventId = event.id,
                                    eventSlug = event.slug,
                                    eventTitle = event.title
                                )
                            )
                            analyticsService.track(AnalyticsEvent.WidgetCreated)
                            val result = Intent().putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                appWidgetId
                            )
                            setResult(RESULT_OK, result)
                            finish()
                        }
                    }
                )
            }
        }
    }
}
