package com.streamatico.polymarketviewer.data.model.data_api

import kotlinx.serialization.Serializable

// Ref: https://docs.polymarket.com/api-reference/builders/get-aggregated-builder-leaderboard
@Serializable
data class LeaderBoardDto(
    val rank: String,
    val proxyWallet: String,
    val userName: String,
    val xUsername: String?  = null,
    val verifiedBadge: Boolean,
    val vol: Double? = null,
    val pnl: Double? = null,
    val profileImage: String?  = null
)