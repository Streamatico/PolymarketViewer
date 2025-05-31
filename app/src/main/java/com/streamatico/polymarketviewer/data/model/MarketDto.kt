package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer // Import the custom serializer
import com.streamatico.polymarketviewer.data.util.JsonUtils
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import kotlin.collections.any

@Serializable
data class MarketDto(
    @SerialName("id") val id: String, // Example: "0x123...abc"
    @SerialName("question") val question: String, // Title/market question
    @SerialName("slug") val slug: String, // Used for URL
    @SerialName("resolutionSource") val resolutionSource: String? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("startDate") val startDate: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("endDate") val endDate: OffsetDateTime? = null,

    @SerialName("image") val imageUrl: String? = null,
    @SerialName("icon") val iconUrl: String? = null,

    @SerialName("description") val description: String? = null,

    @SerialName("outcomes") val outcomesJson: String? = null,
    @SerialName("outcomePrices") val outcomePricesJson: String? = null,

    @SerialName("volumeNum") val volume: Double? = null, // Trading volume in USD (can be null)
    @SerialName("liquidityNum") val liquidity: Double? = null,  // Liquidity in USD (can be null)

    @SerialName("active") val active: Boolean, // Whether market is active
    @SerialName("closed") val closed: Boolean, // Whether closed (event occurred)

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("createdAt") val createdAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updatedAt") val updatedAt: OffsetDateTime? = null,

    @SerialName("new") val isNew: Boolean,
    @SerialName("featured") val isFeatured: Boolean? = null,

    @SerialName("submitted_by") val submittedBy: String? = null,
    @SerialName("archived") val isArchived: Boolean,
    @SerialName("resolvedBy") val resolvedBy: String? = null,
    @SerialName("restricted") val isRestricted: Boolean,

    @SerialName("groupItemTitle") val groupItemTitle: String? = null,
    @SerialName("groupItemThreshold") val groupItemThreshold: Int? = null,
    @SerialName("questionID") val questionId: String? = null,


    @SerialName("hasReviewedDates") val hasReviewedDates: Boolean? = null,

    @SerialName("volume24hr") val volume24hr: Double? = null,
    @SerialName("volume1wk") val volume1wk: Double? = null,
    @SerialName("volume1mo") val volume1mo: Double? = null,
    @SerialName("volume1yr") val volume1yr: Double? = null,

    @SerialName("competitive") val competitive: Double? = null,

    @SerialName("clobTokenIds") val clobTokenIds: String? = null, // List of ERC1155 token ID of conditional token being traded


    @SerialName("acceptingOrders") val acceptingOrders: Boolean? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("acceptingOrdersTimestamp") val acceptingOrdersTimestamp: OffsetDateTime? = null,

    @SerialName("ready") val isReady: Boolean,
    @SerialName("funded") val isFunded: Boolean,
    @SerialName("approved") val isApproved: Boolean,

    @SerialName("lastTradePrice") val lastTradePrice: Double? = null,
    @SerialName("bestBid") val bestBid: Double? = null,
    @SerialName("bestAsk") val bestAsk: Double? = null,
    @SerialName("automaticallyActive") val automaticallyActive: Boolean = false,

    @SerialName("seriesColor") val seriesColor: String? = null,
    @SerialName("showGmpSeries") val showGmpSeries: Boolean = false,
    @SerialName("showGmpOutcome") val showGmpOutcome: Boolean = false,
    @SerialName("manualActivation") val manualActivation: Boolean,

    // Allowed values: "disputed", "resolved", ...
    @SerialName("umaResolutionStatus") val umaResolutionStatus: String? = null,
) {
    val outcomes: List<String> by lazy {
        JsonUtils.parsedJsonList(outcomesJson) ?: emptyList()
    }

    val outcomePrices: List<Double> by lazy {
        JsonUtils.parsedJsonList(outcomePricesJson)?.mapNotNull { it.toDoubleOrNull() ?: 0.0 } ?: emptyList()
    }

    fun getHasPositiveCompetitive(): Boolean {
        return competitive != null && competitive > 0
    }

    val yesPrice: Double? by lazy {
        if(getHasPositiveCompetitive()) {
            if (outcomes.size == 2 && outcomePrices.size == 2) outcomePrices[0] else null
        } else {
            null
        }
    }

    val isBinaryMarket: Boolean by lazy {
        (outcomes.size == 2 && outcomes.any { it.equals("yes", ignoreCase = true) })
    }

    fun getChartLabel(): String {
        //if(isBinaryMarket) return "Yes"
        return getTitleOrDefault("Unknown")
    }

    fun getYesTitle(): String {
        val binaryOutcomeTitle1 = if(outcomes.size == 2) outcomes[0] else null

        if(!binaryOutcomeTitle1.isNullOrEmpty()) return binaryOutcomeTitle1

        if(question.isNotEmpty()) return question

        return "Unknown outcome"
    }

    fun getTitleOrDefault(defaultTitle: String): String {
        val title = groupItemTitle?.trim()

        if(!title.isNullOrEmpty()) return title

        return defaultTitle
    }

    fun getResolutionStatus(): MarketResolutionStatus? {
        return when (umaResolutionStatus) {
            "disputed" -> MarketResolutionStatus.DISPUTED
            "resolved" -> MarketResolutionStatus.RESOLVED
            else -> null
        }
    }
}

enum class MarketResolutionStatus {
    DISPUTED,
    RESOLVED,
}

 fun demoMarketDto(
    id: String = "m-cat",
    question: String = "Who will win the Game Award?",
    slug: String = "m-cat-slug",
    description: String = "Categorical market description",
    active: Boolean = true,
    closed: Boolean = false,
    resolutionSource: String = "Source Cat",
    startDate: OffsetDateTime? = OffsetDateTime.now().minusMonths(2),
    endDate: OffsetDateTime? = OffsetDateTime.now().minusMonths(1),
    volume: Double? = 15000.0,
    liquidity: Double? = 2000.0,
    outcomesJson: String = "[\"Game A\", \"Game B\", \"Game C (Long Name Example)\", \"Game D\", \"Game E\"]",
    outcomePricesJson: String = "[\"0.45\", \"0.30\", \"0.15\", \"0.05\", \"0.05\"]",
    groupItemTitle: String? = null,
    umaResolutionStatus: String? = null,

    createdAt: OffsetDateTime = OffsetDateTime.now(),
    updatedAt: OffsetDateTime? = null,

    lastTradePrice: Double? = null,
    bestBid: Double? = null,
    bestAsk: Double? = null,

    isNew: Boolean = false,
    isFeatured: Boolean? = null,
    submittedBy: String? = null,
    isArchived: Boolean = false,
    resolvedBy: String? = null,
    isRestricted: Boolean = false,

    volume24hr: Double? = null,
    volume1wk: Double? = null,
    volume1mo: Double? = null,
    volume1yr: Double? = null,

    groupItemThreshold: Int? = null,
): MarketDto =
    MarketDto(
        id = id,
        question = question,
        slug = slug,
        description = description,
        active = active,
        closed = closed,
        resolutionSource = resolutionSource,
        startDate = startDate,
        endDate = endDate,
        volume = volume,
        liquidity = liquidity,
        outcomesJson = outcomesJson,
        outcomePricesJson = outcomePricesJson,
        groupItemTitle = groupItemTitle,

        groupItemThreshold = groupItemThreshold,
        submittedBy = submittedBy,
        isNew = isNew,
        isFeatured = isFeatured,
        isArchived = isArchived,
        resolvedBy = resolvedBy,
        isRestricted = isRestricted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isReady = true,
        isFunded = true,
        isApproved = true,
        acceptingOrders = true,
        acceptingOrdersTimestamp = null,
        automaticallyActive = false,
        lastTradePrice = lastTradePrice,
        bestBid = bestBid,
        bestAsk = bestAsk,
        seriesColor = null,
        showGmpSeries = false,
        showGmpOutcome = false,
        manualActivation = false,
        clobTokenIds = null,
        hasReviewedDates = null,
        imageUrl = null,
        iconUrl = null,
        questionId = null,

        volume24hr = volume24hr,
        volume1wk = volume1wk,
        volume1mo = volume1mo,
        volume1yr = volume1yr,

        umaResolutionStatus = umaResolutionStatus,
)
