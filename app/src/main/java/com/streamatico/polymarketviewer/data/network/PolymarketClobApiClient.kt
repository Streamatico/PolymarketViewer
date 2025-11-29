package com.streamatico.polymarketviewer.data.network

import com.streamatico.polymarketviewer.data.model.clob_api.PriceHistoryResponseDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Named

// Class to handle API calls to Polymarket Clob API using Ktor
class PolymarketClobApiClient @Inject constructor(
    @Named(PolymarketHttpClientNames.CLOB_CLIENT) private val client: HttpClient
) {

    /**
     * Fetches price history for a specific market token.
     * Ref: https://docs.polymarket.com/#http-request-30
     */
    suspend fun getPricesHistory(
        marketTokenId: String, // the CLOB token id
        interval: String,      // e.g., "all", "h1", "d1"
        fidelity: Int? = null,  // resolution in minutes
        startTs: Long? = null,  // start timestamp in seconds
        endTs: Long? = null     // end timestamp in seconds

    ): PriceHistoryResponseDto {
        return client.get("prices-history") {
            parameter("market", marketTokenId)
            parameter("interval", interval)
            parameter("fidelity", fidelity)
            parameter("startTs", startTs)
            parameter("endTs", endTs)
        }.body()
    }
}