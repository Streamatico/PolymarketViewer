package com.streamatico.polymarketviewer.data.model.gamma_api

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * DTO for a single comment.
 * Updated based on the actual API response structure.
 */
@Serializable
data class CommentDto(
    @SerialName("id") val id: String,
    @SerialName("body") val body: String? = null, // Changed from text

    // Author/User Info
    @SerialName("profile") val profile: CommentCreatorProfileDto, // Changed from author: AuthorDto?
    @SerialName("userAddress") val userAddress: String? = null, // Added

    // Timestamps
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("createdAt") val createdAt: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updatedAt") val updatedAt: OffsetDateTime? = null, // Added

    // Relationships
    @SerialName("parentEntityType") val parentEntityType: String? = null, // Added (e.g., "Event")
    @SerialName("parentEntityID") val parentEntityID: Long? = null, // Added (Event ID, assuming Long)
    @SerialName("parentCommentID") val parentCommentID: String? = null, // Changed from parentId
    @SerialName("replyAddress") val replyAddress: String? = null, // Added

    // Counts
    @SerialName("reactionCount") val reactionCount: Int? = null, // Changed from likesCount
    @SerialName("reportCount") val reportCount: Int? = null, // Added
    // repliesCount seems not directly available in the root level, maybe calculable?

    // Other fields (reactions omitted for simplicity for now)
    // @SerialName("reactions") val reactions: List<Any>? // Use @SerialName if uncommented
)