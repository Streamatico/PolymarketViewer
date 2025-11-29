package com.streamatico.polymarketviewer.data.model.data_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Ref: https://docs.polymarket.com/api-reference/core/get-total-value-of-a-users-positions
@Serializable
data class UserTradedDto(
    // User Profile Address (0x-prefixed, 40 hex chars)
    @SerialName("user") val user: String,

    @SerialName("traded") val traded: Int? = null,
)