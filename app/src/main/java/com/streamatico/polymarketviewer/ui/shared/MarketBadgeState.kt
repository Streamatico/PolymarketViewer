package com.streamatico.polymarketviewer.ui.shared

import java.time.OffsetDateTime

sealed class MarketBadgeState {
    data object Resolved : MarketBadgeState()
    data object Locked : MarketBadgeState()
    data object Resolving : MarketBadgeState()
    data class ActiveEndsOnDate(val endDate: OffsetDateTime) : MarketBadgeState()
    data object Unknown : MarketBadgeState()

    companion object {
        fun from(
            active: Boolean,
            closed: Boolean,
            endDate: OffsetDateTime?,
            currentTime: OffsetDateTime = OffsetDateTime.now()
        ): MarketBadgeState {
            return when {
                closed -> Resolved
                !active -> Locked
                endDate == null -> Unknown
                endDate <= currentTime -> Resolving
                else -> ActiveEndsOnDate(endDate = endDate)
            }
        }
    }
}
