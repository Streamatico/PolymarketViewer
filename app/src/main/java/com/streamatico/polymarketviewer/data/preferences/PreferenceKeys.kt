package com.streamatico.polymarketviewer.data.preferences

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    // Unique and persistent (across app restarts, but not data clear/reinstall) user ID
    val PREFERENCES_VERSION = intPreferencesKey("preferences_version")
} 