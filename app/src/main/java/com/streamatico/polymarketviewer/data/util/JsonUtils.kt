package com.streamatico.polymarketviewer.data.util

import android.util.Log
import kotlinx.serialization.json.Json

internal object JsonUtils {
    fun parsedJsonList(jsonString: String?): List<String>? {
        if (jsonString != null) {
            try {
                return jsonParser.decodeFromString<List<String>>(jsonString) // Use Kotlinx
            } catch (e: Exception) {
                Log.e("JsonParse", "Failed to parse JSON string: $jsonString", e)
            }
        }

        return null // Return null in case of error
    }
}

// Configure Json parser once at the top level or inside remember where needed
private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }
