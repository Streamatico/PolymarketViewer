package com.streamatico.polymarketviewer.ui.widget.tooling

import com.streamatico.polymarketviewer.domain.model.EventType
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import com.streamatico.polymarketviewer.ui.widget.EventWidgetRenderState
import com.streamatico.polymarketviewer.ui.widget.EventWidgetSelection
import com.streamatico.polymarketviewer.ui.widget.EventWidgetSnapshotBuilder

internal object WidgetPreviewMocks {
    fun previewWidgetState(
        eventType: EventType,
        isEmpty: Boolean = false
    ): EventWidgetRenderState {
        val event = when (eventType) {
            EventType.BinaryEvent -> PreviewMocks.sampleBinaryEvent
            EventType.MultiMarket -> PreviewMocks.sampleEvent1
            EventType.CategoricalMarket -> PreviewMocks.sampleCategoricalEvent
        }

        val selection = EventWidgetSelection(
                eventId = event.id,
                eventSlug = event.slug,
                eventTitle = event.title
            )

        val snapshot = if (isEmpty) null else
            EventWidgetSnapshotBuilder.buildMock(
                event = event
            )

        return EventWidgetRenderState(
            selection = selection,
            snapshot = snapshot,
            disableInteractions = true
        )
    }
}