package com.streamatico.polymarketviewer.data.network

import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.data.model.data_api.LeaderBoardDto
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTotalPositionValueDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTradedDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.parametersOf
import javax.inject.Inject
import javax.inject.Named

// Class to handle API calls to Polymarket Gamma API using Ktor
class PolymarketDataApiClient @Inject constructor(
    @Named(PolymarketHttpClientNames.DATA_CLIENT) private val client: HttpClient
) {
    suspend fun getPositions(
        address: String,
        limit: Int = 20,
        offset: Int = 0,
        sortBy: String? = null,
        sortDirection: String? = null,
        sizeThreshold: Double? = null,
    ): List<UserPositionDto> {
        return client.get("positions") {
            parameter("user", address)
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("sortBy", sortBy)
            parameter("sortDirection", sortDirection)
            parameter("sizeThreshold", sizeThreshold)
        }.body()
    }

    suspend fun getClosedPositions(
        address: String,
        limit: Int = 20,
        offset: Int = 0,
        sortBy: String? = null,
        sortDirection: String? = null
    ): List<UserClosedPositionDto> {
        return client.get("closed-positions") {
            parameter("user", address)
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("sortBy", sortBy)
            parameter("sortDirection", sortDirection)
        }.body()
    }


    // Get total value of a user's positions
    // Ref: https://docs.polymarket.com/api-reference/core/get-total-value-of-a-users-positions
    suspend fun getTotalPositionsValue(
        user: String, // User Profile Address (0x-prefixed, 40 hex chars)
        markets: List<String>? = null
    ): List<UserTotalPositionValueDto> {
        return client.get("value") {
            parameter("user", user)
            if(markets != null) {
                parametersOf("market", markets)
            }
        }.body()
    }

    // Get aggregated builder leaderboard
    // Returns aggregated builder rankings with one entry per builder showing total for the specified time period. Supports pagination.
    // Ref: https://docs.polymarket.com/api-reference/builders/get-aggregated-builder-leaderboard
    suspend fun getLeaderBoard(
        limit: Int = 50,
        offset: Int = 0,
        user: String? = null, // User Profile Address (0x-prefixed, 40 hex chars)
        timePeriod: String? = null,
        orderBy: String? = null,
        category: String? = null,
    ): List<LeaderBoardDto> {
        return client.get("v1/leaderboard") {
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("user", user)
            parameter("timePeriod", timePeriod)
            parameter("orderBy", orderBy)
            parameter("category", category)
        }.body()
    }

    // Get total markets a user has traded
    // Ref: https://docs.polymarket.com/api-reference/misc/get-total-markets-a-user-has-traded
    suspend fun getUserTraded(
        user: String, // User Profile Address (0x-prefixed, 40 hex chars)
    ): UserTradedDto {
        return client.get("traded") {
            parameter("user", user)
        }.body()
    }

    suspend fun getActivity(
        address: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<UserActivityDto> {
        // Assuming the endpoint structure based on typical Polymarket API patterns
        return client.get("activity") {
            parameter("user", address)
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }
}