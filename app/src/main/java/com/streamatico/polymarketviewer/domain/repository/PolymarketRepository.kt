package com.streamatico.polymarketviewer.domain.repository

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

/**
 * Repository interface for getting data from Polymarket API.
 */
interface PolymarketRepository {

    /**
     * Gets list of events (Events).
     * Added filtering/sorting parameters.
     */
    suspend fun getEvents(
        limit: Int? = null,
        offset: Int? = null,
        active: Boolean? = true,
        tagSlug: String? = null,
        archived: Boolean? = false,
        closed: Boolean? = false,
        order: PolymarketEventsSortOrder,
        excludeTagIds: List<Long>? = null,
        ids: List<String>? = null
    ): Result<PaginationDataDto<EventDto>>

    /**
     * Gets details of one market by ID.
     */
    suspend fun getMarketDetails(marketId: String): Result<MarketDto>

    /**
     * Gets details of one event by slug.
     */
    suspend fun getEventDetailsBySlug(eventSlug: String): Result<EventDto>

    /**
     * Gets list of available tags (categories).
     */
    suspend fun getTags(): Result<List<TagDto>>

    /**
     * Gets timeseries data for a market.
     */
    suspend fun getMarketTimeseries(
        marketTokenId: String,
        interval: String,
        resolutionInMinutes: Int?,
        startTimestamp: Long? = null,
        endTimestamp: Long? = null
    ): Result<List<TimeseriesPointDto>>

    /**
     * Gets list of comments for an event.
     */
    suspend fun getComments(
        parentEntity: CommentsParentEntityId,
        limit: Int? = 40,
        offset: Int? = 0,
        holdersOnly: Boolean,
        order: CommentsSortOrder
        //order: String? = "createdAt",
        //ascending: Boolean? = false
        // Add other parameters if needed
    ): Result<List<CommentDto>>

    suspend fun getUserProfile(userAddress: String): Result<UserProfileDto>

    /**
     * Search for events, profiles, and tags (optimized format).
     */
    suspend fun searchPublicOptimized(
        query: String,
        limitPerType: Int = 6,
        eventsStatus: String = "active"
    ): Result<SearchResultOptimizedDto>

    /**
     * Search for events, profiles, and tags (full format with pagination).
     */
    suspend fun searchPublicFull(
        query: String,
        limitPerType: Int = 6,
        eventsStatus: String = "active"
    ): Result<SearchResultDto>

    suspend fun getPositions(
        userAddress: String,
        limit: Int = 10,
        offset: Int = 0
    ): Result<List<UserPositionDto>>

    suspend fun getClosedPositions(
        userAddress: String,
        limit: Int = 10,
        offset: Int = 0
    ): Result<List<UserClosedPositionDto>>

    suspend fun getTotalPositionsValue(
        userAddress: String,
        markets: List<String>? = null
    ): Result<List<UserTotalPositionValueDto>>

    suspend fun getUserLeaderBoard(
        userAddress: String,
    ) : Result<LeaderBoardDto?>

    suspend fun getUserTraded(
        userAddress: String,
    ): Result<UserTradedDto>

    suspend fun getActivity(
        userAddress: String,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<UserActivityDto>>
}

data class CommentsParentEntityId(
    val entityType: CommentsParentEntityType,
    val entityId: String,
)

enum class CommentsParentEntityType(val value: String) {
    Series("Series"),
    Market("market"),
    Event("Event"),
}

enum class CommentsSortOrder {
    NEWEST,
    MOST_LIKED
}

enum class PolymarketEventsSortOrder {
    VOLUME_24H,
    VOLUME_TOTAL,
    LIQUIDITY,
    NEWEST,
    ENDING_SOON,
    COMPETITIVE;

    companion object {
        val DEFAULT_SORT_ORDER = VOLUME_24H
    }
}