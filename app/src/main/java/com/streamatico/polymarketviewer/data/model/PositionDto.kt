package com.streamatico.polymarketviewer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for a single position held by a user, typically embedded within a ProfileDto.
 */
@Serializable
data class PositionDto(
    @SerialName("tokenId") val tokenId: String, // ID of the market outcome token
    @SerialName("positionSize") val positionSize: String? // Size of the position (comes as string)
) 