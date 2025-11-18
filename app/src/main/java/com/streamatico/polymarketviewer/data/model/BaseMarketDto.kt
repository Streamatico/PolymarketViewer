package com.streamatico.polymarketviewer.data.model

import java.time.OffsetDateTime

interface BaseMarketDto {
    val question: String
    val slug: String

    val outcomes: List<String>
    val outcomePrices: List<Double>

    val spread: Double?
    val bestBid: Double?
    val bestAsk: Double?
    val lastTradePrice: Double?

    val oneDayPriceChange: Double?
    val oneWeekPriceChange: Double?
    val oneMonthPriceChange: Double?

    val active: Boolean
    val closed: Boolean
    val isArchived: Boolean

    val groupItemThreshold: Int?
    val groupItemTitle: String?

    val closedTime: OffsetDateTime?

    val umaResolutionStatus: String?
}

enum class MarketResolutionStatus {
    DISPUTED,
    RESOLVED,
}

fun isYesOutcome(outcome: String): Boolean {
    return outcome.equals("yes", ignoreCase = true)
}

fun BaseMarketDto.yesPrice(): Double? {
    return if(outcomes.size == 2 && outcomePrices.size == 2) {
        if(isYesOutcome(outcomes[0])) outcomePrices[0]
        else if(isYesOutcome(outcomes[1])) outcomePrices[1]
        else null
    } else {
        null
    }
}

fun BaseMarketDto.getResolutionStatus(): MarketResolutionStatus? {
    return when (umaResolutionStatus) {
        "disputed" -> MarketResolutionStatus.DISPUTED
        "resolved" -> MarketResolutionStatus.RESOLVED
        else -> null
    }
}

fun BaseMarketDto.getYesTitle(): String {
    val binaryOutcomeTitle1 = if(outcomes.size == 2) outcomes[0] else null

    if(!binaryOutcomeTitle1.isNullOrEmpty()) return binaryOutcomeTitle1

    if(question.isNotEmpty()) return question

    return "Unknown outcome"
}

fun BaseMarketDto.getTitleOrDefault(defaultTitle: String): String {
    val title = groupItemTitle?.trim()

    if(!title.isNullOrEmpty()) return title

    return defaultTitle
}
