package com.streamatico.polymarketviewer.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferenceKeys {
    // Unique and persistent (across app restarts, but not data clear/reinstall) user ID
    val PREFERENCES_VERSION = intPreferencesKey("preferences_version")

    // Analytics opt-out setting (enabled by default)
    val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")

    // First launch flag
    val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")

    // Watchlist event IDs
    val WATCHLIST_EVENT_IDS = androidx.datastore.preferences.core.stringSetPreferencesKey("watchlist_event_ids")
}