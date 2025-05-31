package com.streamatico.polymarketviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for representing tag data from /api/tags/filteredBySlug.
 */
@Serializable
data class TagDto(
    @SerialName("id") val id: String,
    @SerialName("label") val label: String, // Display name (e.g., "Politics")
    @SerialName("slug") val slug: String,   // Identifier for filtering (e.g., "politics")
    @SerialName("forceShow") val forceShow: Boolean? = null,
    @SerialName("forceHide") val forceHide: Boolean? = null,
)