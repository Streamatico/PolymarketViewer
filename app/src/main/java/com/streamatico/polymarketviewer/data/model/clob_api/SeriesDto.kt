package com.streamatico.polymarketviewer.data.model.clob_api

import com.streamatico.polymarketviewer.data.serializers.OffsetDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/** Gamma series metadata is optional; only the ID is needed to load series comments. */
@Serializable
data class SeriesDto (
    @SerialName("id") val id: String,
    @SerialName("ticker") val ticker: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("seriesType") val seriesType: String? = null,
    @SerialName("recurrence") val recurrence: String? = null,
    @SerialName("image") val image: String? = null, // Made nullable as URLs can sometimes be missing
    @SerialName("icon") val icon: String? = null,   // Made nullable

    @SerialName("layout") val layout: String? = null,

    @SerialName("active") val active: Boolean? = null,
    @SerialName("closed") val closed: Boolean? = null,
    @SerialName("archived") val archived: Boolean? = null,
    @SerialName("new") val new: Boolean? = null,
    @SerialName("featured") val featured: Boolean? = null,
    @SerialName("restricted") val restricted: Boolean? = null,

    @SerialName("publishedAt") val publishedAt: String? = null,

    @SerialName("createdBy") val createdBy: String? = null, // Assuming ID as String, potentially nullable
    @SerialName("updatedBy") val updatedBy: String? = null, // Assuming ID as String, potentially nullable

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("createdAt") val createdAt: OffsetDateTime? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("updatedAt") val updatedAt: OffsetDateTime? = null,

    @SerialName("commentsEnabled") val commentsEnabled: Boolean? = null,
    @SerialName("competitive") val competitive: String? = null, // Example shows "0", using String
    @SerialName("volume24hr") val volume24hr: Double? = null,
    @SerialName("volume") val volume: Double? = null,
    @SerialName("liquidity") val liquidity: Double? = null,

    @Serializable(with = OffsetDateTimeSerializer::class)
    @SerialName("startDate") val startDate: OffsetDateTime? = null,

    @SerialName("commentCount") val commentCount: Long? = null
)
