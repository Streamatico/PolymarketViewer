package com.streamatico.polymarketviewer.data.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Serializer for Unix timestamps (seconds since epoch) to OffsetDateTime.
 * Converts Long timestamps to OffsetDateTime in UTC timezone.
 */
object UnixTimestampSerializer : KSerializer<OffsetDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UnixTimestamp", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        // Convert OffsetDateTime to Unix timestamp (seconds)
        val timestamp = value.toEpochSecond()
        encoder.encodeLong(timestamp)
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val timestamp = decoder.decodeLong()
        // Convert Unix timestamp (seconds) to OffsetDateTime in UTC
        return OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp),
            ZoneId.of("UTC")
        )
    }
}
