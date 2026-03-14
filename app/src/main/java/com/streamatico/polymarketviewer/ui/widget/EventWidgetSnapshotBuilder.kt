package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import android.graphics.Bitmap
import coil3.BitmapImage
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.transformations
import coil3.size.Scale
import coil3.transform.RoundedCornersTransformation
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.ui.shared.MarketDisplayRow
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.toDisplayRows
import com.streamatico.polymarketviewer.ui.shared.totalDisplayRowsCount
import java.io.File
import java.time.Instant

internal object EventWidgetSnapshotBuilder {
    suspend fun build(
        context: Context,
        event: BaseEventDto
    ): EventWidgetSnapshot {
        val imageCachePath = event.imageUrl?.let {
            downloadAndCacheImage(context, it, event.id)
        }

        return internalBuild(
            event = event,
            imageCachePath = imageCachePath
        )
    }

    internal fun buildMock(event: BaseEventDto) : EventWidgetSnapshot {
        return internalBuild(
            event = event,
            imageCachePath = null
        )
    }

    private fun internalBuild(
        event: BaseEventDto,
        imageCachePath: String?
    ): EventWidgetSnapshot {
        val rows: List<EventWidgetRow> = buildRows(event)
        val totalRowsCount = buildTotalRowsCount(event)

        val endDateEpochMs = event.endDate?.toInstant()?.toEpochMilli()

        return EventWidgetSnapshot(
            eventId = event.id,
            eventSlug = event.slug,
            eventTitle = event.title,
            eventType = event.eventType,
            closed = event.closed,
            volume = event.volume,
            updatedAtEpochMs = Instant.now().toEpochMilli(),
            endDateEpochMs = endDateEpochMs,
            rows = rows,
            totalRowsCount = totalRowsCount,
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

    private fun buildRows(event: BaseEventDto): List<EventWidgetRow> =
        event.toDisplayRows(MAX_CACHED_ROWS).map { it.toWidgetRow() }

    private fun buildTotalRowsCount(event: BaseEventDto): Int =
        event.totalDisplayRowsCount()
}

private const val MAX_CACHED_ROWS = 50
private const val IMAGE_SIZE_PX = 150  // Reduced from 200 to match smaller 40dp icon size
private const val IMAGE_CORNER_RADIUS = 24f

private fun MarketDisplayRow.toWidgetRow() = EventWidgetRow(
    title = title,
    price = price,
    displayValue = resolvedOutcome ?: UiFormatter.formatPriceAsPercentage(price),
    resolutionStatus = resolutionStatus
)