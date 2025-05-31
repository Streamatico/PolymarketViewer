package com.streamatico.polymarketviewer.data.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    // Standard ISO formatter
    private val isoFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        // Always serialize using the standard ISO format
        val stringValue = value.format(isoFormatter)
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): OffsetDateTime {
        val originalStringValue = decoder.decodeString()
        // Replace the first space with 'T' if present after the date part (index 10)
        val stringValue = if (originalStringValue.length > 10 && originalStringValue[10] == ' ') {
            originalStringValue
                .replaceFirst(" ", "T")
                .let {
                    if(it.endsWith("+00")) it.replace("+00", "Z")
                    else it
                }

        } else {
            originalStringValue
        }

        // Attempt parsing with the standard ISO formatter after potential replacement
        try {
            return OffsetDateTime.parse(stringValue, isoFormatter)
        } catch (e: Exception) {
            // Handle parsing error (e.g., log it, throw a custom exception, etc.)
            throw IllegalArgumentException("Invalid date format: $stringValue", e)
        }
    }
}