package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime



/**
 * DTO for representing optimized event data from Gamma API.
 */
@Serializable
data class OptimizedEventDto(
    @SerialName("id") override val id: String,
    @SerialName("title") override val title: String,
    @SerialName("slug") override val slug: String,

    @SerialName("image") override val imageUrl: String? = null, // Image URL

    @SerialName("active") override val active: Boolean,
    @SerialName("closed") override val closed: Boolean,
    @SerialName("archived") override val archived: Boolean = false,
    @SerialName("ended") override val ended: Boolean = false,
    @SerialName("negRisk") override val negRisk: Boolean = false,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("closedTime") override val closedTime: OffsetDateTime? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("startDate") override val startDate: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("endDate") override val endDate: OffsetDateTime? = null,

    @SerialName("markets") internal val rawMarkets: List<OptimizedMarketDto>, // Make internal

    @SerialName("tags") override val tags: List<TagDto>? = null, // List of event tags (categories)

    @SerialName("sortBy") override val sortBy: String? = null,

    @SerialName("gameId") override val gameId: Long? = null,

    override val volume: Double? = null,

    // Add other fields as needed (volume24hr, etc.)
) : BaseEventDto {
    val markets: List<OptimizedMarketDto> by lazy {
        val activeMarkets = rawMarkets.filter { it.active } // Filter only active markets
        activeMarkets
    }

    override val baseMarkets: List<BaseMarketDto> by lazy {
        markets
    }

    // TODO: Remove duplicated code (EventDto and OptimizedEventDto)
    override val eventType: EventType by lazy {
        when {
            rawMarkets.size == 1 && rawMarkets[0].isBinaryMarket -> EventType.BinaryEvent
            rawMarkets.size == 1 || gameId != null -> EventType.CategoricalMarket
            else -> EventType.MultiMarket
        }
    }
}

fun demoOptimizedEventDto(
    id: String,
    title: String = "Demo Event",
    slug: String = "demo-event",
    imageUrl: String? = null,
    active: Boolean = true,
    closed: Boolean = false,
    startDate: OffsetDateTime? = OffsetDateTime.now().minusDays(2),
    endDate: OffsetDateTime? = OffsetDateTime.now().minusDays(1),
    rawMarkets: List<OptimizedMarketDto> = emptyList(),
): OptimizedEventDto {
    return OptimizedEventDto(
        id = id,
        title = title,
        slug = slug,
        imageUrl = imageUrl,
        active = active,
        closed = closed,
        startDate = startDate,
        endDate = endDate,
        rawMarkets = rawMarkets,
    )
}