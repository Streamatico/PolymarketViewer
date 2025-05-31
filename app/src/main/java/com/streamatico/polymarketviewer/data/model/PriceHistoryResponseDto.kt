package com.streamatico.polymarketviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the root object returned by the /prices-history API endpoint.
 */
@Serializable
data class PriceHistoryResponseDto(
    @SerialName("history") val history: List<TimeseriesPointDto>
)