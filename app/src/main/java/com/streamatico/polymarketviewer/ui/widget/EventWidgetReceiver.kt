package com.streamatico.polymarketviewer.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class EventWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            EventWidgetGeneratedPreviewPublisher.publishIfSupported(context.applicationContext)
        } else
            if (intent.action == AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED) {
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        EventWidget().updateAll(context.applicationContext)
                        EventWidgetUpdater.enqueueImmediate(context.applicationContext)
                    }
                }
            } else if (intent.action == ACTION_PIN_WIDGET) {
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                Log.d(TAG, "Pin widget action: ${intent.action} (widgetId: $widgetId)")

                if (widgetId > 0) {
                    val selection = EventWidgetPinRequester.getSavedSelection(intent)

                    if (selection != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            EventWidgetUpdater.saveSelection(
                                context = context,
                                appWidgetId = widgetId,
                                selection = selection
                            )
                        }
                    }
                }
            } else {
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                Log.d(TAG, "Unhandled widget action: ${intent.action} (widgetId: $widgetId)")
            }
    }

    companion object {
        const val ACTION_PIN_WIDGET = "com.polymarket.viewer.action.PIN_WIDGET"
        private const val TAG = "EventWidgetReceiver"
    }
}
