package com.streamatico.polymarketviewer.domain.repository

import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.MarketDto
import com.streamatico.polymarketviewer.data.model.TagDto
import com.streamatico.polymarketviewer.data.model.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.CommentDto
import com.streamatico.polymarketviewer.data.model.UserProfileDto
import kotlin.Result

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
        //ascending: Boolean? = false,
        eventId: String? = null,
        excludeTagId: Long? = null
    ): Result<List<EventDto>>

    /**
     * Gets details of one market by ID.
     */
    suspend fun getMarketDetails(marketId: String): Result<MarketDto>

    /**
     * Gets details of one event by ID.
     */
    suspend fun getEventDetails(eventId: String): Result<EventDto>

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
        //limit: Int,
        //timestampGt: Long?
    ): Result<List<TimeseriesPointDto>>

    /**
     * Gets list of comments for an event.
     */
    suspend fun getComments(
        eventId: String,
        limit: Int? = 40,
        offset: Int? = 0,
        holdersOnly: Boolean? = null,
        order: String? = "createdAt",
        ascending: Boolean? = false
        // Add other parameters if needed
    ): Result<List<CommentDto>>

    suspend fun getUserProfile(address: String): Result<UserProfileDto>
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