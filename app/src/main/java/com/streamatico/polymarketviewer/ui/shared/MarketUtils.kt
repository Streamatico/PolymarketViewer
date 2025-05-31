package com.streamatico.polymarketviewer.ui.shared

import com.streamatico.polymarketviewer.data.model.EventMarketsSortBy
import com.streamatico.polymarketviewer.data.model.MarketDto
import com.streamatico.polymarketviewer.data.model.MarketResolutionStatus

fun List<MarketDto>.sortedByViewPriority(sortByEnum: EventMarketsSortBy): List<MarketDto> {
    return when(sortByEnum) {
        EventMarketsSortBy.None -> this
            .sortedBy { if(it.yesPrice == null) 1 else 0 }

        EventMarketsSortBy.Price -> this
            .sortedWith(
                // Sort by yesPrice descending
                // Move resolved markets to the end
                compareBy<MarketDto> { if (it.getResolutionStatus() != MarketResolutionStatus.RESOLVED) 0 else 1 }
                    .thenByDescending { it.yesPrice }
            )
    }
}