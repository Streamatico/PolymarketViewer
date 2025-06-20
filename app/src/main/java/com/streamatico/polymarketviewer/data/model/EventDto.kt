package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * DTO for representing event data from Gamma API.
 */
@Serializable
data class EventDto(
    @SerialName("id") val id: String,
    @SerialName("title") internal val title: String, // Make internal if possible
    @SerialName("slug") val slug: String,
    @SerialName("description") internal val description: String? = null, // Make internal
    @SerialName("category") val category: String? = null,

    @SerialName("icon") val iconUrl: String? = null,   // Icon URL
    @SerialName("image") val imageUrl: String? = null, // Image URL
    @SerialName("showMarketImages") val showMarketImages: Boolean = true,

    // NBA market showAllOutcomes = false
    @SerialName("showAllOutcomes") val showAllOutcomes: Boolean = true,

    @SerialName("active") val active: Boolean,
    @SerialName("closed") val closed: Boolean,
    @SerialName("archived") val archived: Boolean = false,
    @SerialName("new") val new: Boolean = false,
    @SerialName("restricted") val restricted: Boolean = false,

    @SerialName("volume") val volume: Double? = null,
    @SerialName("liquidity") val liquidity: Double? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("createdAt") val createdAt: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updatedAt") val updatedAt: OffsetDateTime? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("creationDate") val creationDate: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("startDate") val startDate: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("endDate") val endDate: OffsetDateTime? = null,

    @SerialName("resolution_source") val resolutionSource: String? = null,
    @SerialName("markets") internal val rawMarkets: List<MarketDto>, // Make internal
    @SerialName("featured") val featured: Boolean? = null,
    @SerialName("featuredOrder") val featuredOrder: Int? = null,
    @SerialName("tags") val tags: List<TagDto>? = null, // List of event tags (categories)

    @SerialName("competitive") val competitive: Double? = null,
    @SerialName("commentCount") val commentCount: Long = 0,

    @SerialName("sortBy") val sortBy: String? = null,

    @SerialName("series") val series: List<SeriesDto>? = null,

    // Add other fields as needed (volume24hr, etc.)
){
    val markets: List<MarketDto> by lazy {
        val activeMarkets = rawMarkets.filter { it.active } // Filter only active markets

        // TODO: Detect if the event is a sport market more accurately
        val isSportMarket = !showAllOutcomes && series != null && series.isNotEmpty()

        if(isSportMarket) {
            // TODO: Handle sport markets (example: NBA id=23714)
            activeMarkets.take(1)
        } else {
            activeMarkets
        }
    }

    val eventType: EventType by lazy {
        when {
            rawMarkets.size == 1 && rawMarkets[0].isBinaryMarket -> EventType.BinaryEvent
            rawMarkets.size == 1 -> EventType.CategoricalMarket
            else -> EventType.MultiMarket
        }
    }

    val sortByEnum: EventMarketsSortBy
        get() = when (sortBy) {
            "price" -> EventMarketsSortBy.Price
            else -> EventMarketsSortBy.None
        }
}

enum class EventType {
    BinaryEvent,
    CategoricalMarket,
    MultiMarket,
}

enum class EventMarketsSortBy {
    None,
    Price
}

fun demoEventDto(
    id: String,
    title: String = "Demo Event",
    slug: String = "demo-event",
    description: String = "This is a demo event.",
    category: String = "Demo Category",
    imageUrl: String? = null,
    iconUrl: String? = null,
    active: Boolean = true,
    closed: Boolean = false,
    volume: Double = 1000.0,
    liquidity: Double = 500.0,
    startDate: OffsetDateTime? = OffsetDateTime.now().minusDays(2),
    endDate: OffsetDateTime? = OffsetDateTime.now().minusDays(1),
    resolutionSource: String? = null,
    rawMarkets: List<MarketDto> = emptyList(),
    featured: Boolean? = true,
    featuredOrder: Int? = 1
): EventDto {
    return EventDto(
        id = id,
        title = title,
        slug = slug,
        description = description,
        category = category,
        imageUrl = imageUrl,
        iconUrl = iconUrl,
        active = active,
        closed = closed,
        volume = volume,
        liquidity = liquidity,
        startDate = startDate,
        endDate = endDate,
        resolutionSource = resolutionSource,
        rawMarkets = rawMarkets,
        featured = featured,
        featuredOrder = featuredOrder
    )
}