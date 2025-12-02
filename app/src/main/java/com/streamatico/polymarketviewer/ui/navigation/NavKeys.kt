package com.streamatico.polymarketviewer.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

data object NavKeys {
    @Serializable data object EventList : NavKey
    @Serializable data object Search : NavKey
    @Serializable data class EventDetail(val eventSlug: String) : NavKey
    @Serializable data class MarketDetail(val marketId: String) : NavKey
    @Serializable data class UserProfile(val userAddress: String) : NavKey
    @Serializable data object About : NavKey
}