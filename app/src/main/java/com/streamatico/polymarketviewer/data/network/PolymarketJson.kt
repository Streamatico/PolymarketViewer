package com.streamatico.polymarketviewer.data.network

import kotlinx.serialization.json.Json

// Shared with contract tests so they exercise the same decoding rules as the HTTP clients.
internal val polymarketJson = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}
