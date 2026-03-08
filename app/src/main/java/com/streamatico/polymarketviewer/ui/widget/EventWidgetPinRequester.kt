package com.streamatico.polymarketviewer.ui.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto

internal object EventWidgetPinRequester {
    private const val EXTRA_WIDGET_PREFILL_EVENT_ID = "extra_widget_prefill_event_id"
    private const val EXTRA_WIDGET_PREFILL_EVENT_SLUG = "extra_widget_prefill_event_slug"
    private const val EXTRA_WIDGET_PREFILL_EVENT_TITLE = "extra_widget_prefill_event_title"

    suspend fun requestPinWidget(context: Context, event: EventDto): Boolean {
        val previewState = EventWidgetRenderState(
            selection = EventWidgetSelection(
                eventId = event.id,
                eventSlug = event.slug,
                eventTitle = event.title
            ),
            snapshot = EventWidgetSnapshotBuilder.build(context, event),
            disableInteractions = true
        )

        return GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
            receiver = EventWidgetReceiver::class.java,
            preview = EventWidget.createWithPreview(previewState),
            successCallback = createPinWidgetSuccessCallback(context, event)
            //previewState = DpSize(245.dp, 115.dp)
        )
    }

    private fun createPinWidgetSuccessCallback(context: Context, event: EventDto): PendingIntent {
        val extras = Bundle().apply {
            putString(EXTRA_WIDGET_PREFILL_EVENT_ID, event.id)
            putString(EXTRA_WIDGET_PREFILL_EVENT_SLUG, event.slug)
            putString(EXTRA_WIDGET_PREFILL_EVENT_TITLE, event.title)
        }

        val intent = Intent(context, EventWidgetReceiver::class.java)
        intent.action = EventWidgetReceiver.ACTION_PIN_WIDGET
        intent.putExtras(extras)

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun getSavedSelection(intent: Intent): EventWidgetSelection? {
        val eventId = intent.getStringExtra(EXTRA_WIDGET_PREFILL_EVENT_ID)
        val eventSlug = intent.getStringExtra(EXTRA_WIDGET_PREFILL_EVENT_SLUG)
        val eventTitle = intent.getStringExtra(EXTRA_WIDGET_PREFILL_EVENT_TITLE) ?: "Unknown"

        if (eventId == null || eventSlug == null) return null

        return EventWidgetSelection(
            eventId = eventId,
            eventSlug = eventSlug,
            eventTitle = eventTitle
        )
    }
}