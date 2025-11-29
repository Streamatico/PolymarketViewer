package com.streamatico.polymarketviewer.data.model.gamma_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface PolymarketUserProfile {
    val proxyWallet: String // User's wallet address
    val profileImage: String?
    val displayUsernamePublic: Boolean?
    val pseudonym: String?
    val name: String?
}

fun PolymarketUserProfile.getDisplayName(): String {
    val name = this.name
    val pseudonym = this.pseudonym

    return if (displayUsernamePublic == true && !name.isNullOrBlank()) {
        // Display "pseudonym" instead of "name" when "name" is too long (behaviour like on Polymarket Web)
        if(name.length > 30 && !pseudonym.isNullOrEmpty() && pseudonym.length > 2)
            pseudonym
        else
            name
    } else if (!pseudonym.isNullOrBlank()) {
        pseudonym
    } else {
        // Fallback or generate placeholder if needed
        "User ${proxyWallet.takeLast(4)}" // Example fallback
    }
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
    @SerialName("positions") val positions: List<ProfilePositionDto>? = null // Added positions list
): PolymarketUserProfile

@Serializable
data class ProfilePositionDto(
    @SerialName("tokenId") val tokenId: String, // ID of the market outcome token
    @SerialName("positionSize") val positionSize: String? // Size of the position (comes as string)
)
