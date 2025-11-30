package com.streamatico.polymarketviewer.data.model.data_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO representing a user's position in a market from Polymarket Data API.
 * Endpoint: GET https://data-api.polymarket.com/positions (active)
 *
 * Based on real API responses.
 */
@Serializable
data class UserPositionDto(
    // User information
    @SerialName("proxyWallet") val proxyWallet: String? = null, // User's proxy wallet address

    // Market identifiers
    @SerialName("asset") val asset: String? = null, // Token ID (conditionId)
    @SerialName("conditionId") val conditionId: String? = null, // Condition ID for the market

    // Market information
    @SerialName("title") val title: String? = null, // Market title/question
    @SerialName("slug") val slug: String? = null, // Market slug
    @SerialName("icon") val icon: String? = null, // Market icon URL

    @SerialName("outcome") val outcome: String? = null, // Outcome name (e.g., "Yes", "No")
    @SerialName("outcomeIndex") val outcomeIndex: Int? = null, // Index of the outcome
    @SerialName("oppositeOutcome") val oppositeOutcome: String? = null,
    @SerialName("oppositeAsset") val oppositeAsset: String? = null,

    // Event information
    @SerialName("eventId") val eventId: String,
    @SerialName("eventSlug") val eventSlug: String,
    @SerialName("endDate") val endDate: String? = null, // End date as string

    // Position size and value (mainly for ACTIVE positions)
    @SerialName("size") val size: Double? = null, // Number of shares/tokens held (active only)
    @SerialName("avgPrice") val avgPrice: Double? = null, // Average entry price per share
    @SerialName("initialValue") val initialValue: Double? = null, // Initial cost basis in USD (active only)
    @SerialName("currentValue") val currentValue: Double? = null, // Current value in USD (active only)
    @SerialName("curPrice") val curPrice: Double? = null, // Current market price per share

    // Profit/Loss metrics
    @SerialName("cashPnl") val cashPnl: Double? = null, // Cash profit/loss in USD (active positions)
    @SerialName("percentPnl") val percentPnl: Double? = null, // Percentage profit/loss as decimal (active positions)
    @SerialName("realizedPnl") val realizedPnl: Double? = null, // Realized PnL (mainly for closed positions)
    @SerialName("percentRealizedPnl") val percentRealizedPnl: Double? = null,

    // Trading metrics
    @SerialName("totalBought") val totalBought: Double? = null, // Total amount bought

    // Position state flags
    @SerialName("redeemable") val redeemable: Boolean? = null,
    @SerialName("mergeable") val mergeable: Boolean? = null,
    @SerialName("negativeRisk") val negativeRisk: Boolean? = null,
) {
    // Computed properties for backward compatibility with UI
    val value: Double
        get() = currentValue ?: (size?.let { it * (curPrice ?: 0.0) } ?: 0.0)

    val price: Double
        get() = curPrice ?: 0.0

    val pnl: Double?
        get() = cashPnl ?: realizedPnl
}