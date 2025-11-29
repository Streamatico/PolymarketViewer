package com.streamatico.polymarketviewer.ui.shared

import com.streamatico.polymarketviewer.data.model.gamma_api.EventMarketsSortBy
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice

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

        .sortedBy { it.groupItemThreshold }
}