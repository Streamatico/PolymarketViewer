package com.streamatico.polymarketviewer.data.model.data_api

import com.streamatico.polymarketviewer.data.serializers.UnixTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * DTO representing a user's closed position in a market from Polymarket Data API.
 * Endpoint: GET https://data-api.polymarket.com/closed-positions (closed)
 *
 * Based on real API responses.
 */
@Serializable
data class UserClosedPositionDto(
    // User information
    @SerialName("proxyWallet") val proxyWallet: String? = null, // User's proxy wallet address

    // Market identifiers
    @SerialName("asset") val asset: String? = null, // Token ID (conditionId)
    @SerialName("conditionId") val conditionId: String? = null, // Condition ID for the market

    // Market information
    @SerialName("title") val title: String? = null, // Market title/question
    @SerialName("slug") val slug: String? = null, // Market slug
    @SerialName("icon") val icon: String? = null, // Market icon URL

    @SerialName("outcome") val outcome: String, // Outcome name (e.g., "Yes", "No")
    @SerialName("outcomeIndex") val outcomeIndex: Int? = null, // Index of the outcome
    @SerialName("oppositeOutcome") val oppositeOutcome: String? = null,
    @SerialName("oppositeAsset") val oppositeAsset: String? = null,

    // Event information
    @SerialName("eventSlug") val eventSlug: String,
    @SerialName("endDate") val endDate: String? = null, // End date as string

    // Position size and value (mainly for ACTIVE positions)
    @SerialName("avgPrice") val avgPrice: Double, // Average entry price per share
    @SerialName("curPrice") val curPrice: Double? = null, // Current market price per share

    // Profit/Loss metrics
    @SerialName("realizedPnl") val realizedPnl: Double, // Realized PnL (mainly for closed positions)

    // Trading metrics
    @SerialName("totalBought") val totalBought: Double, // Total amount bought

    // Timestamp (for closed positions)
    @Serializable(with = UnixTimestampSerializer::class)
    @SerialName("timestamp") val timestamp: OffsetDateTime
)