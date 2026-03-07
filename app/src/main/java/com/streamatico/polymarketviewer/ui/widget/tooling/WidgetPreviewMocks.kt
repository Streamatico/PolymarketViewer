package com.streamatico.polymarketviewer.ui.widget.tooling

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.domain.model.EventType
import com.streamatico.polymarketviewer.ui.widget.EventWidgetRenderState
import com.streamatico.polymarketviewer.ui.widget.EventWidgetRow
import com.streamatico.polymarketviewer.ui.widget.EventWidgetSelection
import com.streamatico.polymarketviewer.ui.widget.EventWidgetSnapshot

internal object WidgetPreviewMocks {
    fun previewWidgetState(
        eventType: EventType,
        size: DpSize = DpSize(320.dp, 180.dp)
    ): EventWidgetRenderState {
        val now = System.currentTimeMillis()

        val rows = when (eventType) {
            EventType.CategoricalMarket -> listOf(
                EventWidgetRow(title = "Trump", value = "39%"),
                EventWidgetRow(title = "Harris", value = "35%"),
                EventWidgetRow(title = "Newsom", value = "9%"),
                EventWidgetRow(title = "Other", value = "17%")
            )
            EventType.MultiMarket -> listOf(
                EventWidgetRow(title = "Ethereum", value = "31%"),
                EventWidgetRow(title = "Solana", value = "26%"),
                EventWidgetRow(title = "Bitcoin", value = "22%"),
                EventWidgetRow(title = "Sui", value = "5%"),
                EventWidgetRow(title = "Tron", value = "4%"),
                EventWidgetRow(title = "Litecoin", value = "4%"),
                EventWidgetRow(title = "Other", value = "1%")
            )
            EventType.BinaryEvent -> listOf(
                EventWidgetRow(title = "Yes", value = "64%"),
                EventWidgetRow(title = "No", value = "36%")
            )
        }

        val title = when (eventType) {
            EventType.CategoricalMarket -> "Who will win the 2028 U.S. presidential election?"
            EventType.MultiMarket -> "Which L1 chain will lead by market cap growth this quarter?"
            EventType.BinaryEvent -> "Will Bitcoin close above $120k by the end of 2026?"
        }

        return EventWidgetRenderState(
            size = size,
            selection = EventWidgetSelection(
                eventId = "preview-event-id",
                eventSlug = "bitcoin-120k-2026",
                eventTitle = title
            ),
            snapshot = EventWidgetSnapshot(
                eventId = "preview-event-id",
                eventSlug = "bitcoin-120k-2026",
                eventTitle = title,
                eventType = eventType,
                closed = false,
                volume = 12_800_000.0,
                updatedAtEpochMs = now - 12 * 60 * 1000,
                endDateEpochMs = now + 5L * 24 * 60 * 60 * 1000,
                rows = rows,
                totalRowsCount = rows.size,
                binaryYesPrice = if (eventType == EventType.BinaryEvent) 0.64 else null,
                imageCachePath = null
            ),
            disableInteractions = true
        )
    }
}