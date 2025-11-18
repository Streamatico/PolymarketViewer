package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class OptimizedMarketDto(
    @SerialName("question") override val question: String, // Title/market question
    @SerialName("slug") override val slug: String, // Used for URL

    @SerialName("outcomes") override val outcomes: List<String>,
    @SerialName("outcomePrices") override val outcomePrices: List<Double>,

    @SerialName("spread") override val spread: Double? = null,
    @SerialName("bestBid") override val bestBid: Double? = null,
    @SerialName("bestAsk") override val bestAsk: Double? = null,
    @SerialName("lastTradePrice") override val lastTradePrice: Double? = null,

    @SerialName("oneDayPriceChange") override val oneDayPriceChange: Double? = null,
    @SerialName("oneWeekPriceChange") override val oneWeekPriceChange: Double? = null,
    @SerialName("oneMonthPriceChange") override val oneMonthPriceChange: Double? = null,

    @SerialName("active") override val active: Boolean, // Whether market is active
    @SerialName("closed") override val closed: Boolean, // Whether closed (event occurred)
    @SerialName("archived") override val isArchived: Boolean,

    @SerialName("groupItemThreshold") override val groupItemThreshold: Int? = null,
    @SerialName("groupItemTitle") override val groupItemTitle: String? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("closedTime") override val closedTime: OffsetDateTime? = null,

    override val umaResolutionStatus: String? = null, // Not applicable for optimized DTO
): BaseMarketDto {
    val isBinaryMarket: Boolean by lazy {
        (outcomes.size == 2 && outcomes.any { isYesOutcome(it) })
    }
}

fun demoOptimizedMarketDto(
    question: String = "Who will win the Game Award?",
    slug: String = "m-cat-slug",
    active: Boolean = true,
    closed: Boolean = false,

    outcomes: List<String> = listOf("Game A", "Game B", "Game C (Long Name Example)", "Game D", "Game E"),
    outcomePrices: List<Double> = listOf(0.45, 0.30, 0.15, 0.05, 0.05),
    groupItemTitle: String? = null,

    lastTradePrice: Double? = null,
    bestBid: Double? = null,
    bestAsk: Double? = null,

    isArchived: Boolean = false,
): OptimizedMarketDto =
    OptimizedMarketDto(
        question = question,
        slug = slug,
        active = active,
        closed = closed,
        groupItemTitle = groupItemTitle,

        isArchived = isArchived,
        lastTradePrice = lastTradePrice,
        bestBid = bestBid,
        bestAsk = bestAsk,
        outcomes = outcomes,
        outcomePrices = outcomePrices,
    )
