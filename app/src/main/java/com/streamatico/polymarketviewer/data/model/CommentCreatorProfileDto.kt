package com.streamatico.polymarketviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface PolymarketUserProfile {
    val proxyWallet: String // User's wallet address
    val profileImage: String?
    val displayUsernamePublic: Boolean?
    val pseudonym: String?
    val name: String?
}

/**
 * DTO for user profile information embedded in comments.
 * Based on the actual API response structure.
 */
@Serializable // Add Serializable annotation
data class CommentCreatorProfileDto(
    // Usernames
    @SerialName("name") override val name: String? = null, // User-set name
    @SerialName("pseudonym") override val pseudonym: String? = null, // Generated pseudonym
    @SerialName("displayUsernamePublic") override val displayUsernamePublic: Boolean?,

    // Addresses
    @SerialName("userAddress") val userAddress: String? = null, // Address associated with the comment itself
    @SerialName("baseAddress") val baseAddress: String? = null, // User's primary address
    @SerialName("proxyWallet") override val proxyWallet: String,

    // Profile details
    @SerialName("profileImage") override val profileImage: String? = null, // Avatar URL
    @SerialName("bio") val bio: String? = null,

    // User positions
    // Assuming PositionDto will also be made @Serializable
    @SerialName("positions") val positions: List<PositionDto>? = null // Added positions list
): PolymarketUserProfile
{
    /**
     * Helper to get the most appropriate display name.
     */
    // No need for @Transient on function
    fun getDisplayName(): String {
        return if (displayUsernamePublic == true && !name.isNullOrBlank()) {
            name
        } else if (!pseudonym.isNullOrBlank()) {
            pseudonym
        } else if (!baseAddress.isNullOrBlank()){
             // Fallback to shortened address if no name available
             baseAddress.take(6) + "..." + baseAddress.takeLast(4)
        } else {
            "Anonymous"
        }
    }
} 