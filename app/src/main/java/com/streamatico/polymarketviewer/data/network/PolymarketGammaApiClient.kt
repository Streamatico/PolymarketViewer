package com.streamatico.polymarketviewer.data.network

import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PaginationDataDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultOptimizedDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments



// Class to handle API calls to Polymarket Gamma API using Ktor
class PolymarketGammaApiClient(
    private val client: HttpClient
) {

    suspend fun getEvents(
        limit: Int = 20,
        offset: Int = 0,
        active: Boolean? = true,
        archived: Boolean? = false,
        closed: Boolean? = false,
        new: Boolean? = null,
        featured: Boolean? = null,
        restricted: Boolean? = null,
        tagSlug: String? = null,
        order: String? = "volume",
        ascending: Boolean? = false,
        excludeTagIds: List<Long>? = null,
        slugList: List<String>? = null,
        idList: List<String>? = null
    ): PaginationDataDto<EventDto> {
        return client.get("events/pagination") {
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("active", active)
            parameter("archived", archived)
            parameter("closed", closed)
            parameter("new", new)
            parameter("featured", featured)
            parameter("restricted", restricted)
            parameter("tag_slug", tagSlug)
            parameter("order", order)
            parameter("ascending", ascending)
            excludeTagIds?.forEach { parameter("exclude_tag_id", it) }
            slugList?.forEach { parameter("slug", it) }
            idList?.forEach { parameter("id", it) }
        }.body()
    }

    suspend fun getMarketDetails(marketId: String): MarketDto {
        return client.get("markets") {
            url {
                appendPathSegments(marketId)
            }
        }.body()
    }

    suspend fun getTags(): List<TagDto> {
        // Use the full URL directly for this specific endpoint
        return client.get("https://polymarket.com/api/tags/filteredBySlug") {
            parameter("tag", "all")
            parameter("status", "active")
        }.body()
    }

    suspend fun getEventDetailsBySlug(eventSlug: String): EventDto {
        return client.get("events/slug") {
            url {
                appendPathSegments(eventSlug)
            }
        }.body()
    }

    // Ref: https://docs.polymarket.com/api-reference/comments/list-comments
    suspend fun getComments(
        parentEntityId: String,
        parentEntityType: String, // Event, Series, market
        limit: Int = 40,
        offset: Int = 0,
        ascending: Boolean? = false,
        order: String? = "createdAt",
        holdersOnly: Boolean? = null,
        getPositions: Boolean? = true,
        getReports: Boolean? = true
    ): List<CommentDto> {
        return client.get("comments") {
            parameter("parent_entity_id", parentEntityId)
            parameter("parent_entity_type", parentEntityType)
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("ascending", ascending)
            parameter("order", order)
            parameter("holders_only", holdersOnly)
            parameter("get_positions", getPositions)
            parameter("get_reports", getReports)
        }.body()
    }

    suspend fun getUserProfile(address: String): UserProfileDto {
        // Use the full URL directly for this specific endpoint
        return client.get("https://polymarket.com/api/profile/userData") {
            parameter("address", address)
        }.body()
    }

    suspend fun searchPublicOptimized(
        query: String,
        limitPerType: Int = 6,
        searchTags: Boolean = true,
        searchProfiles: Boolean = true,
        cache: Boolean = true,
        eventsStatus: String = "active"
    ): SearchResultOptimizedDto {
        return client.get("public-search") {
            parameter("q", query)
            parameter("optimized", true)
            parameter("limit_per_type", limitPerType)
            parameter("type", "events")
            parameter("search_tags", searchTags)
            parameter("search_profiles", searchProfiles)
            parameter("cache", cache)
            parameter("events_status", eventsStatus)
        }.body()
    }

    suspend fun searchPublicFull(
        query: String,
        limitPerType: Int = 6,
        searchTags: Boolean = true,
        searchProfiles: Boolean = true,
        cache: Boolean = true,
        eventsStatus: String = "active"
    ): SearchResultDto {
        return client.get("public-search") {
            parameter("q", query)
            parameter("optimized", false)
            parameter("limit_per_type", limitPerType)
            parameter("type", "events")
            parameter("search_tags", searchTags)
            parameter("search_profiles", searchProfiles)
            parameter("cache", cache)
            parameter("events_status", eventsStatus)
        }.body()
    }
}