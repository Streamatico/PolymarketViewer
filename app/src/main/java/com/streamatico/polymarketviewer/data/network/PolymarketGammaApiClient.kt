package com.streamatico.polymarketviewer.data.network

import com.streamatico.polymarketviewer.data.model.CommentDto
import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.MarketDto
import com.streamatico.polymarketviewer.data.model.TagDto
import com.streamatico.polymarketviewer.data.model.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments
import javax.inject.Inject
import javax.inject.Named

// Class to handle API calls to Polymarket Gamma API using Ktor
class PolymarketGammaApiClient @Inject constructor(
    @Named("GammaClient") private val client: HttpClient
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
        excludeTagId: Long? = null,
        eventId: String? = null // Note: API might ignore this non-standard parameter
    ): List<EventDto> {
        return client.get("events") {
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
            parameter("exclude_tag_id", excludeTagId)
            parameter("event_id", eventId) // Include potentially ignored param
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

    suspend fun getEventDetails(eventIdOrSlug: String): EventDto {
        // Use string interpolation directly in the path
        return client.get("events") {
            url {
                appendPathSegments(eventIdOrSlug)
            }
            // No need for url block here if baseUrl is set in defaultRequest
        }.body()
    }

    suspend fun getComments(
        eventId: String,
        parentEntityType: String = "Event",
        limit: Int = 40,
        offset: Int = 0,
        ascending: Boolean? = false,
        order: String? = "createdAt",
        holdersOnly: Boolean? = null,
        getPositions: Boolean? = true,
        getReports: Boolean? = true
    ): List<CommentDto> {
        return client.get("comments") {
            parameter("parent_entity_id", eventId)
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
} 