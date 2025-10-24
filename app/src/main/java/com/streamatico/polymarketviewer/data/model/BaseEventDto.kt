package com.streamatico.polymarketviewer.data.model

import kotlinx.serialization.SerialName
import java.time.OffsetDateTime

interface BaseEventDto {
    val id: String
    val title: String
    val slug: String

    val imageUrl: String?

    val active: Boolean
    val closed: Boolean
    val archived: Boolean
    val ended: Boolean
    val negRisk: Boolean

    val closedTime: OffsetDateTime?

    val startDate: OffsetDateTime?
    val endDate: OffsetDateTime?

    val volume: Double?

    val tags: List<TagDto>?

    val eventType: EventType
    val sortByEnum: EventMarketsSortBy

    val baseMarkets: List<BaseMarketDto>
}