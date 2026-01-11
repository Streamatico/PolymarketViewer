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


class PolymarketRepositoryImpl(
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
        excludeTagIds: List<Long>?,
        ids: List<String>?
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
                excludeTagIds = excludeTagIds,
                idList = ids
            )
        }
    }

    override suspend fun getMarketDetails(marketId: String): Result<MarketDto> {
        if(marketId.isBlank()) {
            return Result.failure(IllegalArgumentException("marketId is blank"))
        }
        return safeApiCall { gammaApiClient.getMarketDetails(marketId = marketId) }
    }

    override suspend fun getTags(): Result<List<TagDto>> {
        return safeApiCall { gammaApiClient.getTags() }
    }

    override suspend fun getEventDetailsBySlug(eventSlug: String): Result<EventDto> {
        if(eventSlug.isBlank()) {
            return Result.failure(IllegalArgumentException("Event slug is blank"))
        }
        return safeApiCall { gammaApiClient.getEventDetailsBySlug(eventSlug = eventSlug) }
    }

    override suspend fun getMarketTimeseries(
        marketTokenId: String,
        interval: String,
        resolutionInMinutes: Int?,
        startTimestamp: Long?,
        endTimestamp: Long?
    ): Result<List<TimeseriesPointDto>> {
        if(marketTokenId.isBlank()) {
            return Result.failure(IllegalArgumentException("MarketTokenId is blank"))
        }

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
        if(parentEntity.entityId.isBlank()) {
            return Result.failure(IllegalArgumentException("ParentEntity.entityId is blank"))
        }

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

    override suspend fun getUserProfile(userAddress: String): Result<UserProfileDto> {
        if(userAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("User address is blank"))
        }

        return safeApiCall { gammaApiClient.getUserProfile(userAddress) }
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
        userAddress: String,
        limit: Int,
        offset: Int
    ): Result<List<UserPositionDto>> {
        if(userAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("User address is blank"))
        }

        return safeApiCall {
            dataApiClient.getPositions(
                address = userAddress,
                limit = limit,
                offset = offset,
                sortBy = "CURRENT",
                sortDirection = "DESC",
                sizeThreshold = 0.1
            )
        }
    }

    override suspend fun getClosedPositions(
        userAddress: String,
        limit: Int,
        offset: Int
    ): Result<List<UserClosedPositionDto>> {
        return safeApiCall {
            dataApiClient.getClosedPositions(
                address = userAddress,
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
        if(userAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("User address is blank"))
        }

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
        if(userAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("User address is blank"))
        }

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
        if(userAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("User address is blank"))
        }

        return safeApiCall {
            dataApiClient.getUserTraded(
                user = userAddress
            )
        }
    }

    override suspend fun getActivity(
        userAddress: String,
        limit: Int,
        offset: Int
    ): Result<List<UserActivityDto>> {
        if(userAddress.isBlank()) {
            return Result.failure(IllegalArgumentException("User address is blank"))
        }

        return safeApiCall {
            dataApiClient.getActivity(
                address = userAddress,
                limit = limit,
                offset = offset
            )
        }
    }
}