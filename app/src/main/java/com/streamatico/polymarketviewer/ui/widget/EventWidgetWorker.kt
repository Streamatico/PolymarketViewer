package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil3.BitmapImage
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.transformations
import coil3.size.Scale
import coil3.transform.RoundedCornersTransformation
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventType
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.shared.MarketDisplayRow
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.toDisplayRows
import com.streamatico.polymarketviewer.ui.shared.totalDisplayRowsCount
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
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
private const val IMAGE_SIZE_PX = 150  // Reduced from 200 to match smaller 40dp icon size
private const val IMAGE_CORNER_RADIUS = 24f

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
        val snapshot = buildSnapshot(context, event, selection)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[SNAPSHOT_KEY] = EventWidgetSnapshotSerializer.encode(snapshot)
        }
        EventWidget().update(context, glanceId)
    }

    private suspend fun buildSnapshot(
        context: Context,
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
        val imageCachePath = event.imageUrl?.let {
            downloadAndCacheImage(context, it, event.id)
        }
        val endDateEpochMs = event.endDate?.toInstant()?.toEpochMilli()

        return EventWidgetSnapshot(
            eventId = selection.eventId,
            eventSlug = selection.eventSlug,
            eventTitle = event.title,
            eventType = event.eventType.name,
            closed = event.closed,
            volume = event.volume,
            updatedAtEpochMs = Instant.now().toEpochMilli(),
            endDateEpochMs = endDateEpochMs,
            rows = rows,
            totalRowsCount = totalRowsCount,
            binaryYesPrice = binaryYesPrice,
            imageCachePath = imageCachePath
        )
    }

    private suspend fun downloadAndCacheImage(
        context: Context,
        imageUrl: String,
        eventId: String
    ): String? = runCatching {
        val imageLoader = SingletonImageLoader.get(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(IMAGE_SIZE_PX, IMAGE_SIZE_PX)
            .scale(Scale.FILL)
            .transformations(RoundedCornersTransformation(IMAGE_CORNER_RADIUS))
            .allowHardware(false) // required to read pixels and save to file
            .build()

        val bitmap = ((imageLoader.execute(request) as? SuccessResult)?.image as? BitmapImage)?.bitmap//?.roundedWithBackground(IMAGE_CORNER_RADIUS)
            ?: return@runCatching null

        val file = File(context.cacheDir, "widget_images/$eventId.png")
        file.parentFile?.mkdirs()
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
        }
        file.absolutePath
    }.getOrNull()

    private fun buildRows(event: BaseEventDto, limit: Int): List<EventWidgetRow> =
        event.toDisplayRows(limit).map { it.toWidgetRow() }

    private fun buildTotalRowsCount(event: BaseEventDto): Int =
        event.totalDisplayRowsCount()
}

private fun MarketDisplayRow.toWidgetRow() = EventWidgetRow(
    title = title,
    value = resolvedOutcome ?: UiFormatter.formatPriceAsPercentage(price),
    isResolved = resolutionStatus == MarketResolutionStatus.RESOLVED
)
