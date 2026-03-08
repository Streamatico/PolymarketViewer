package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal class EventWidgetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    override suspend fun doWork(): Result {
        EventWidgetRefresher.refreshAll(applicationContext)
        return Result.success()
    }
}

internal object EventWidgetRefresher : KoinComponent {
    private val repository: PolymarketRepository by inject()

    suspend fun refreshAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(EventWidget::class.java)
        if (glanceIds.isEmpty()) return

        glanceIds.forEach { glanceId -> refresh(context, glanceId) }
    }

    suspend fun refresh(context: Context, glanceId: GlanceId) {
        val state = getAppWidgetState(
            context = context,
            definition = PreferencesGlanceStateDefinition,
            glanceId = glanceId
        )
        val selection = state.readSelection() ?: return

        val event = repository.getEventDetailsBySlug(selection.eventSlug).getOrNull() ?: return
        val snapshot = EventWidgetSnapshotBuilder.build(context, event)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[SNAPSHOT_KEY] = EventWidgetSnapshotSerializer.encode(snapshot)
        }
        EventWidget().update(context, glanceId)
    }
}
