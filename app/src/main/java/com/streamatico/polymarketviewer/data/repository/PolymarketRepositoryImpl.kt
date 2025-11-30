package com.streamatico.polymarketviewer.data.repository

import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.data_api.LeaderBoardDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PaginationDataDto
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultOptimizedDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.data.model.clob_api.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTotalPositionValueDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTradedDto
import com.streamatico.polymarketviewer.data.network.PolymarketClobApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketDataApiClient
import com.streamatico.polymarketviewer.data.network.PolymarketGammaApiClient
import com.streamatico.polymarketviewer.domain.repository.CommentsParentEntityId
import com.streamatico.polymarketviewer.domain.repository.CommentsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import javax.inject.Inject

class PolymarketRepositoryImpl @Inject constructor(
    private val gammaApiClient: PolymarketGammaApiClient,
    private val clobApiClient: PolymarketClobApiClient,
    private val dataApiClient: PolymarketDataApiClient,
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
        excludeTagIds: List<Long>?
    ): Result<PaginationDataDto<EventDto>> {
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
                excludeTagIds = excludeTagIds
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
        return safeApiCall { gammaApiClient.getEventDetails(eventId = eventId) }
    }

    override suspend fun getEventDetailsBySlug(eventSlug: String): Result<EventDto> {
        return safeApiCall { gammaApiClient.getEventDetailsBySlug(eventSlug = eventSlug) }
    }

    override suspend fun getMarketTimeseries(
        marketTokenId: String,
        interval: String,
        resolutionInMinutes: Int?,
        startTimestamp: Long?,
        endTimestamp: Long?
    ): Result<List<TimeseriesPointDto>> {
        return safeApiCall {
            clobApiClient.getPricesHistory(
                marketTokenId = marketTokenId,
                interval = interval,
                fidelity = resolutionInMinutes,
                startTs = startTimestamp,
                endTs = endTimestamp
            )
        }.map { responseDto -> responseDto.history }
    }

    override suspend fun getComments(
        parentEntity: CommentsParentEntityId,
        limit: Int?,
        offset: Int?,
        holdersOnly: Boolean,
        order: CommentsSortOrder,
    ): Result<List<CommentDto>> {
        val resultOrder: String
        val resultAscending: Boolean

        when(order) {
            CommentsSortOrder.NEWEST -> {
                resultOrder = "createdAt"
                resultAscending = false
            }
            CommentsSortOrder.MOST_LIKED -> {
                resultOrder = "reactionCount"
                resultAscending = false
            }
        }

        return safeApiCall {
            gammaApiClient.getComments(
                parentEntityId = parentEntity.entityId,
                parentEntityType = parentEntity.entityType.value,
                limit = limit ?: 40,
                offset = offset ?: 0,
                holdersOnly = holdersOnly,
                order = resultOrder,
                ascending = resultAscending,
                getPositions = true,
                getReports = true
            )
        }
    }

    override suspend fun getUserProfile(address: String): Result<UserProfileDto> {
        return safeApiCall { gammaApiClient.getUserProfile(address) }
    }

    override suspend fun searchPublicOptimized(
        query: String,
        limitPerType: Int,
        eventsStatus: String
    ): Result<SearchResultOptimizedDto> {
        return safeApiCall {
            gammaApiClient.searchPublicOptimized(
                query = query,
                limitPerType = limitPerType,
                eventsStatus = eventsStatus
            )
        }
    }

    override suspend fun searchPublicFull(
        query: String,
        limitPerType: Int,
        eventsStatus: String
    ): Result<SearchResultDto> {
        return safeApiCall {
            gammaApiClient.searchPublicFull(
                query = query,
                limitPerType = limitPerType,
                eventsStatus = eventsStatus
            )
        }
    }

    override suspend fun getPositions(
        address: String,
        limit: Int,
        offset: Int
    ): Result<List<UserPositionDto>> {
        return safeApiCall {
            dataApiClient.getPositions(
                address = address,
                limit = limit,
                offset = offset,
                sortBy = "CURRENT",
                sortDirection = "DESC",
                sizeThreshold = 0.1
            )
        }
    }

    override suspend fun getClosedPositions(
        address: String,
        limit: Int,
        offset: Int
    ): Result<List<UserClosedPositionDto>> {
        return safeApiCall {
            dataApiClient.getClosedPositions(
                address = address,
                limit = limit,
                offset = offset,
                sortBy = "realizedpnl",
                sortDirection = "DESC"
            )
        }
    }

    override suspend fun getTotalPositionsValue(
        userAddress: String,
        markets: List<String>?
    ): Result<List<UserTotalPositionValueDto>> {
        return safeApiCall {
            dataApiClient.getTotalPositionsValue(
                user = userAddress,
                markets = markets
            )
        }
    }

    override suspend fun getUserLeaderBoard(
        userAddress: String,
    ) : Result<LeaderBoardDto?> {
        val result = safeApiCall {
            dataApiClient.getLeaderBoard(
                user = userAddress
            )
        }

        return result.fold(
            onSuccess = {
                Result.success(it.firstOrNull())
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    override suspend fun getUserTraded(
        userAddress: String,
    ): Result<UserTradedDto> {
        return safeApiCall {
            dataApiClient.getUserTraded(
                user = userAddress
            )
        }
    }

    override suspend fun getActivity(
        address: String,
        limit: Int,
        offset: Int
    ): Result<List<UserActivityDto>> {
        return safeApiCall {
            dataApiClient.getActivity(
                address = address,
                limit = limit,
                offset = offset
            )
        }
    }
}