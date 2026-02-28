package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventType
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.shared.MarketDisplayRow
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.toDisplayRows
import com.streamatico.polymarketviewer.ui.shared.totalDisplayRowsCount
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Instant

internal class EventWidgetWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    override suspend fun doWork(): Result {
        EventWidgetRefresher.refreshAll(applicationContext)
        return Result.success()
    }
}

private const val MAX_CACHED_ROWS = 50

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
        val snapshot = buildSnapshot(event, selection)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[SNAPSHOT_KEY] = EventWidgetSnapshotSerializer.encode(snapshot)
        }
        EventWidget().update(context, glanceId)
    }

    private fun buildSnapshot(
        event: BaseEventDto,
        selection: EventWidgetSelection
    ): EventWidgetSnapshot {
        val rows = buildRows(event, MAX_CACHED_ROWS)
        val totalRowsCount = buildTotalRowsCount(event)
        val binaryYesPrice = if (event.eventType == EventType.BinaryEvent) {
            event.baseMarkets.firstOrNull()?.yesPrice()
        } else {
            null
        }
        return EventWidgetSnapshot(
            eventId = selection.eventId,
            eventSlug = selection.eventSlug,
            eventTitle = event.title,
            eventType = event.eventType.name,
            closed = event.closed,
            updatedAtEpochMs = Instant.now().toEpochMilli(),
            rows = rows,
            totalRowsCount = totalRowsCount,
            binaryYesPrice = binaryYesPrice
        )
    }

    private fun buildRows(event: BaseEventDto, limit: Int): List<EventWidgetRow> =
        event.toDisplayRows(limit).map { it.toWidgetRow() }

    private fun buildTotalRowsCount(event: BaseEventDto): Int =
        event.totalDisplayRowsCount()
}

private fun MarketDisplayRow.toWidgetRow() = EventWidgetRow(
    title = title,
    value = if (isResolved && resolvedOutcome != null) resolvedOutcome
            else UiFormatter.formatPriceAsPercentage(price),
    isResolved = isResolved
)
