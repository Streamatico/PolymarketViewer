package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer // Import the custom serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

// Assuming PolymarketUserProfile interface fields are compatible or the interface itself is handled.

// Represents the user data fetched from the profile API
@Serializable
data class UserProfileDto(
    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("createdAt") val createdAt: OffsetDateTime, // Consider parsing to OffsetDateTime if needed
    @SerialName("proxyWallet") override val proxyWallet: String, // User's wallet address
    @SerialName("profileImage") override val profileImage: String? = null,
    @SerialName("displayUsernamePublic") override val displayUsernamePublic: Boolean? = null,
    @SerialName("pseudonym") override val pseudonym: String? = null,
    @SerialName("name") override val name: String? = null,
    @SerialName("users") val users: List<UserAssociationDto>? = null // Additional user info if available
) : PolymarketUserProfile {
    // Helper function to get the display name, similar to CommentDto.ProfileDto
    // No need for @Transient on functions
    fun getDisplayName(): String {
        return if (displayUsernamePublic == true && !name.isNullOrBlank()) {
            name
        } else if (!pseudonym.isNullOrBlank()) {
            pseudonym
        } else {
            // Fallback or generate placeholder if needed
            "User ${proxyWallet.takeLast(4)}" // Example fallback
        }
    }
}

// Represents the nested 'users' object in the profile API response
@Serializable
data class UserAssociationDto(
    @SerialName("id") val id: String?,
    @SerialName("creator") val creator: Boolean?,
    @SerialName("mod") val mod: Boolean?
) 