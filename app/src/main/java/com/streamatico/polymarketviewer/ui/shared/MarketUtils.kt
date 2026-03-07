package com.streamatico.polymarketviewer.ui.shared

import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolvedOutcome
import com.streamatico.polymarketviewer.data.model.gamma_api.getTitleOrDefault
import com.streamatico.polymarketviewer.data.model.gamma_api.getYesTitle
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice
import com.streamatico.polymarketviewer.domain.model.EventMarketsSortBy
import com.streamatico.polymarketviewer.domain.model.EventType

fun <T : BaseMarketDto> List<T>.sortedByViewPriority(sortByEnum: EventMarketsSortBy): List<T> {
    return when(sortByEnum) {
        EventMarketsSortBy.None -> this
            .sortedWith(
                // Sort by yesPrice descending
                // Move resolved markets to the end
                compareBy<T> { if (it.getResolutionStatus() != MarketResolutionStatus.RESOLVED) 0 else 1 }
                    //.thenByDescending { if(it.yesPrice() == null) 1 else 0 }
                    .thenBy { it.groupItemThreshold }
            )

        EventMarketsSortBy.Price -> this
            .sortedWith(
                // Sort by yesPrice descending
                // Move resolved markets to the end
                compareBy<T> { if (it.getResolutionStatus() != MarketResolutionStatus.RESOLVED) 0 else 1 }
                    .thenByDescending { it.yesPrice() ?: 0.0 }
            )
    }
}

fun List<BaseMarketDto>.sortedForShortView(limit: Int): List<BaseMarketDto> {
    return this
        //.filter { !it.closed }

        .sortedByViewPriority(EventMarketsSortBy.Price)

        .take(limit)

        .sortedWith (
            compareBy<BaseMarketDto> { it.isResolved() }
                .thenBy { it.groupItemThreshold  }
        )
}

private fun BaseMarketDto.isResolved(): Boolean {
    return getResolutionStatus() == MarketResolutionStatus.RESOLVED
}


data class MarketDisplayRow(
    val title: String,
    val price: Double?,
    val resolutionStatus: MarketResolutionStatus?,
    val resolvedOutcome: String? = null
)

fun BaseEventDto.toDisplayRows(limit: Int? = null): List<MarketDisplayRow> {
    val markets = baseMarkets
    if (markets.isEmpty()) return emptyList()

    return when (eventType) {
        EventType.BinaryEvent -> listOf(markets.first().toBinaryDisplayRow())

        EventType.CategoricalMarket -> {
            val mainMarket = markets.firstOrNull { it.groupItemThreshold == 0 } ?: markets.first()
            mainMarket.toCategoricalDisplayRows(limit)
        }

        EventType.MultiMarket -> {
            val sortedMarkets = if(limit == null) {
                markets.sortedByViewPriority(sortByEnum)
            } else {
                // Short view
                markets.sortedForShortView(limit)
            }
            val isSingleMarket = sortedMarkets.size == 1
            sortedMarkets.map { it.toMultiMarketDisplayRow(isSingleMarket) }
        }
    }
}

fun BaseEventDto.totalDisplayRowsCount(): Int {
    val markets = baseMarkets
    if (markets.isEmpty()) return 0

    return when (eventType) {
        EventType.BinaryEvent -> 1
        EventType.CategoricalMarket -> {
            val mainMarket = markets.firstOrNull { it.groupItemThreshold == 0 } ?: markets.first()
            mainMarket.outcomes.size
        }
        EventType.MultiMarket -> {
            val categoricalMarket = markets.firstOrNull { it.outcomes.size > 2 }
            categoricalMarket?.outcomes?.size ?: markets.size
        }
    }
}

private fun BaseMarketDto.toBinaryDisplayRow(): MarketDisplayRow {
    return MarketDisplayRow(
        title = getYesTitle(),
        price = yesPrice(),
        resolutionStatus = getResolutionStatus(),
        resolvedOutcome = getResolvedOutcome()
    )
}

private fun BaseMarketDto.toCategoricalDisplayRows(limit: Int? = null): List<MarketDisplayRow> {
    val resolutionStatus = getResolutionStatus()

    return outcomes
        .mapIndexed { index, outcome -> outcome to outcomePrices.getOrNull(index) }
        .sortedWith(compareByDescending { it.second ?: -1.0 })
        .take(limit ?: Int.MAX_VALUE)
        .map { (outcome, price) -> MarketDisplayRow(title = outcome, price = price, resolutionStatus = resolutionStatus) }
}

private fun BaseMarketDto.toMultiMarketDisplayRow(isSingle: Boolean): MarketDisplayRow {
    return MarketDisplayRow(
        title = if (isSingle) getYesTitle() else getTitleOrDefault(question),
        price = yesPrice(),
        resolutionStatus = getResolutionStatus(),
        resolvedOutcome = getResolvedOutcome()
    )
}