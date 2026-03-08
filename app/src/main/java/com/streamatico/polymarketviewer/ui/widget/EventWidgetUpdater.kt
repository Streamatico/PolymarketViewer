package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

internal object EventWidgetUpdater {
    private const val UNIQUE_WORK_NAME = "EventWidgetRefresh"

    fun enqueuePeriodic(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<EventWidgetWorker>(
            EVENT_WIDGET_REFRESH_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun enqueueImmediate(context: Context) {
        val request = OneTimeWorkRequestBuilder<EventWidgetWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WIDGET_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun saveSelection(context: Context, appWidgetId: Int, selection: EventWidgetSelection) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[EVENT_ID_KEY] = selection.eventId
            prefs[EVENT_SLUG_KEY] = selection.eventSlug
            prefs[EVENT_TITLE_KEY] = selection.eventTitle
            prefs.remove(SNAPSHOT_KEY)
        }
        EventWidget().update(context, glanceId)
        enqueueImmediate(context)
    }
}

internal const val EVENT_WIDGET_REFRESH_MINUTES = 15L
private const val IMMEDIATE_WIDGET_WORK_NAME = "event_widget_immediate"