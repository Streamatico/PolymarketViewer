package com.streamatico.polymarketviewer.data.model

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class SeriesDto (
    @SerialName("id") val id: String,
    @SerialName("ticker") val ticker: String,
    @SerialName("slug") val slug: String,
    @SerialName("title") val title: String,
    @SerialName("seriesType") val seriesType: String,
    @SerialName("recurrence") val recurrence: String,
    @SerialName("image") val image: String? = null, // Made nullable as URLs can sometimes be missing
    @SerialName("icon") val icon: String? = null,   // Made nullable

    @SerialName("layout") val layout: String? = null,

    @SerialName("active") val active: Boolean,
    @SerialName("closed") val closed: Boolean,
    @SerialName("archived") val archived: Boolean,
    @SerialName("new") val new: Boolean? = null,
    @SerialName("featured") val featured: Boolean? = null,
    @SerialName("restricted") val restricted: Boolean? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("publishedAt") val publishedAt: OffsetDateTime? = null,

    @SerialName("createdBy") val createdBy: String? = null, // Assuming ID as String, potentially nullable
    @SerialName("updatedBy") val updatedBy: String? = null, // Assuming ID as String, potentially nullable

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("createdAt") val createdAt: OffsetDateTime?,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updatedAt") val updatedAt: OffsetDateTime?,

    @SerialName("commentsEnabled") val commentsEnabled: Boolean? = null,
    @SerialName("competitive") val competitive: String? = null, // Example shows "0", using String
    @SerialName("volume24hr") val volume24hr: Double? = null,
    @SerialName("volume") val volume: Double? = null,
    @SerialName("liquidity") val liquidity: Double? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("startDate") val startDate: OffsetDateTime? = null,

    @SerialName("commentCount") val commentCount: Long?
)