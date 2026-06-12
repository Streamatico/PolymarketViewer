package com.streamatico.polymarketviewer.ui.widget

import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedMarketDto
import com.streamatico.polymarketviewer.ui.shared.toDisplayRows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EventWidgetSnapshotBuilderTest {
    @Test
    fun `multi market snapshot preserves price order when cached rows exceed visible rows`() {
        val event = OptimizedEventDto(
            id = "world-cup-winner",
            title = "World Cup Winner",
            slug = "world-cup-winner",
            active = true,
            closed = false,
            sortBy = "price",
            rawMarkets = listOf(
                market(title = "Spain", price = 0.17, groupItemThreshold = 0),
                market(title = "England", price = 0.10, groupItemThreshold = 1),
                market(title = "France", price = 0.16, groupItemThreshold = 2),
                market(title = "Portugal", price = 0.11, groupItemThreshold = 3),
                market(title = "Brazil", price = 0.08, groupItemThreshold = 4),
            )
        )

        val snapshot = EventWidgetSnapshotBuilder.buildMock(event)
        val canonicalDisplayOrder = event.toDisplayRows().map { it.title }

        assertEquals(
            listOf("Spain", "France", "Portugal", "England", "Brazil"),
            canonicalDisplayOrder
        )
        assertEquals(canonicalDisplayOrder, snapshot.rows.map { it.title })
    }

    private fun market(
        title: String,
        price: Double,
        groupItemThreshold: Int
    ): OptimizedMarketDto =
        OptimizedMarketDto(
            question = title,
            slug = title.lowercase(),
            outcomes = listOf("Yes", "No"),
            outcomePrices = listOf(price, 1 - price),
            active = true,
            closed = false,
            isArchived = false,
            groupItemTitle = title,
            groupItemThreshold = groupItemThreshold
        )
}
