package com.streamatico.polymarketviewer.data.repository

import com.streamatico.polymarketviewer.data.model.CommentDto
import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.MarketDto
import com.streamatico.polymarketviewer.data.model.TagDto
import com.streamatico.polymarketviewer.data.model.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.UserProfileDto
import com.streamatico.polymarketviewer.data.network.PolymarketClobApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketGammaApiClient
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import javax.inject.Inject

class PolymarketRepositoryImpl @Inject constructor(
    private val gammaApiClient: PolymarketGammaApiClient,
    private val clobApiClient: PolymarketClobApiClient
) : PolymarketRepository {

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall.invoke())
        } catch (e: RedirectResponseException) {
            Result.failure(Exception("Redirect Error: ${e.response.status.value}. ${e.message}", e))
        } catch (e: ClientRequestException) {
            Result.failure(Exception("Client Error: ${e.response.status.value}. ${e.message}", e))
        } catch (e: ServerResponseException) {
            Result.failure(Exception("Server Error: ${e.response.status.value}. ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEvents(
        limit: Int?,
        offset: Int?,
        active: Boolean?,
        tagSlug: String?,
        archived: Boolean?,
        closed: Boolean?,
        order: PolymarketEventsSortOrder,
        eventId: String?,
        excludeTagId: Long?
    ): Result<List<EventDto>> {
        val orderString = when (order) {
            PolymarketEventsSortOrder.VOLUME_24H -> "volume24hr"
            PolymarketEventsSortOrder.VOLUME_TOTAL -> "volume"
            PolymarketEventsSortOrder.LIQUIDITY -> "liquidity"
            PolymarketEventsSortOrder.NEWEST -> "startDate"
            PolymarketEventsSortOrder.ENDING_SOON -> "endDate"
            PolymarketEventsSortOrder.COMPETITIVE -> "competitive"
        }

        val isAscending = when(order) {
            PolymarketEventsSortOrder.ENDING_SOON -> true
            else -> false
        }

        return safeApiCall {
            gammaApiClient.getEvents(
                limit = limit ?: 20,
                offset = offset ?: 0,
                active = active,
                tagSlug = tagSlug,
                archived = archived,
                closed = closed,
                order = orderString,
                ascending = isAscending,
                eventId = eventId,
                excludeTagId = excludeTagId
            )
        }
    }

    override suspend fun getMarketDetails(marketId: String): Result<MarketDto> {
        return safeApiCall { gammaApiClient.getMarketDetails(marketId = marketId) }
    }

    override suspend fun getTags(): Result<List<TagDto>> {
        return safeApiCall { gammaApiClient.getTags() }
    }

    override suspend fun getEventDetails(eventId: String): Result<EventDto> {
        return safeApiCall { gammaApiClient.getEventDetails(eventIdOrSlug = eventId) }
    }

    override suspend fun getMarketTimeseries(
        marketTokenId: String,
        interval: String,
        resolutionInMinutes: Int?
    ): Result<List<TimeseriesPointDto>> {
        return safeApiCall { clobApiClient.getPricesHistory(marketTokenId, interval, resolutionInMinutes) }
            .map { responseDto -> responseDto.history }
    }

    override suspend fun getComments(
        eventId: String,
        limit: Int?,
        offset: Int?,
        holdersOnly: Boolean?,
        order: String?,
        ascending: Boolean?
    ): Result<List<CommentDto>> {
        return safeApiCall {
            gammaApiClient.getComments(
                eventId = eventId,
                limit = limit ?: 40,
                offset = offset ?: 0,
                holdersOnly = holdersOnly,
                order = order ?: "createdAt",
                ascending = ascending,
                getPositions = true,
                getReports = true
            )
        }
    }

    override suspend fun getUserProfile(address: String): Result<UserProfileDto> {
        return safeApiCall { gammaApiClient.getUserProfile(address) }
    }
} 