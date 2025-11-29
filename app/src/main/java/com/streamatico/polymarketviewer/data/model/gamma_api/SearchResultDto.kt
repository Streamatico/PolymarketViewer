package com.streamatico.polymarketviewer.data.model.gamma_api

import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for representing optimized search results from /public-search endpoint with optimized=true.
 * Returns events, profiles, and tags that match the search query in a simplified format.
 */
@Serializable
data class SearchResultOptimizedDto(
    @SerialName("events") val events: List<OptimizedEventDto>? = null,
    @SerialName("profiles") val profiles: List<UserProfileDto>? = null,
    @SerialName("tags") val tags: List<TagDto>? = null,
    @SerialName("hasMore") val hasMore: Boolean
)

/**
 * DTO for representing full search results from /public-search endpoint with optimized=false.
 * Returns events, profiles, and tags with detailed pagination information.
 */
@Serializable
data class SearchResultDto(
    @SerialName("events") val events: List<EventDto>? = null,
    @SerialName("profiles") val profiles: List<UserProfileDto>? = null,
    @SerialName("tags") val tags: List<TagDto>? = null,
    @SerialName("pagination") val pagination: PaginationDto
)
