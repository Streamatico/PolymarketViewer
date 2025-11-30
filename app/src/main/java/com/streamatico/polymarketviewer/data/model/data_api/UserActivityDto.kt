package com.streamatico.polymarketviewer.data.model.data_api

import com.streamatico.polymarketviewer.data.serializers.UnixTimestampSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * DTO representing a user's activity (trade, split, merge, etc.) from Polymarket Data API.
 * Endpoint: GET https://data-api.polymarket.com/activity
 *
 * Based on real API responses.
 */
@Serializable
data class UserActivityDto(
    // User information
    @SerialName("proxyWallet") val proxyWallet: String? = null,

    // Transaction information
    @Serializable(with = UnixTimestampSerializer::class)
    @SerialName("timestamp") val timestamp: OffsetDateTime, // Unix timestamp converted to OffsetDateTime
    @SerialName("transactionHash") val transactionHash: String? = null,

    // Market/Asset information
    @SerialName("conditionId") val conditionId: String? = null,
    @SerialName("asset") val asset: String,
    @SerialName("title") val title: String,
    @SerialName("slug") val slug: String? = null,
    @SerialName("icon") val icon: String? = null,
    @SerialName("eventSlug") val eventSlug: String,
    @SerialName("outcome") val outcome: String,
    @SerialName("outcomeIndex") val outcomeIndex: Int? = null,

    // Activity type and details
    @SerialName("type") val type: String, // "TRADE", "SPLIT", "MERGE", "REDEEM", etc.
    @SerialName("side") val side: String? = null, // "BUY" or "SELL" (for trades)
    @SerialName("size") val size: Double, // Number of shares/tokens
    @SerialName("usdcSize") val usdcSize: Double? = null, // USDC value
    @SerialName("price") val price: Double, // Price per share

    // User profile information (may be included in response)
    @SerialName("name") val name: String? = null,
    @SerialName("pseudonym") val pseudonym: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("profileImage") val profileImage: String? = null,
    @SerialName("profileImageOptimized") val profileImageOptimized: String? = null
) {
    // Computed property for backward compatibility
    val value: Double
        get() = usdcSize ?: (size * price)
}