package com.streamatico.polymarketviewer.data.model.clob_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single data point from the /timeseries API endpoint.
 */
@Serializable
data class TimeseriesPointDto(
    @SerialName("t") val timestamp: Long, // Unix timestamp in seconds
    @SerialName("p") val close: Double // Closing price as a string (Now Double)
)