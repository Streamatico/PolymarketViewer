package com.streamatico.polymarketviewer.ui.widget

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val EVENT_ID_KEY = stringPreferencesKey("widget_event_id")
internal val EVENT_SLUG_KEY = stringPreferencesKey("widget_event_slug")
internal val EVENT_TITLE_KEY = stringPreferencesKey("widget_event_title")
internal val SNAPSHOT_KEY = stringPreferencesKey("widget_snapshot")

@Serializable
internal data class EventWidgetRow(
    val title: String,
    val value: String,
    val isResolved: Boolean = false
)

@Serializable
internal data class EventWidgetSnapshot(
    val eventId: String,
    val eventSlug: String,
    val eventTitle: String,
    val eventType: String,
    val closed: Boolean,
    val updatedAtEpochMs: Long,
    val rows: List<EventWidgetRow>,
    val totalRowsCount: Int = 0,
    val binaryYesPrice: Double? = null
)

internal data class EventWidgetSelection(
    val eventId: String,
    val eventSlug: String,
    val eventTitle: String
)

internal object EventWidgetSnapshotSerializer {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun encode(snapshot: EventWidgetSnapshot): String = json.encodeToString(
        EventWidgetSnapshot.serializer(),
        snapshot
    )

    fun decode(value: String?): EventWidgetSnapshot? = value?.let {
        runCatching { json.decodeFromString(EventWidgetSnapshot.serializer(), it) }.getOrNull()
    }
}

internal fun Preferences.readSelection(): EventWidgetSelection? {
    val eventId = this[EVENT_ID_KEY]
    val eventSlug = this[EVENT_SLUG_KEY]
    val eventTitle = this[EVENT_TITLE_KEY]

    if (eventId.isNullOrBlank() || eventSlug.isNullOrBlank()) {
        return null
    }

    return EventWidgetSelection(
        eventId = eventId,
        eventSlug = eventSlug,
        eventTitle = eventTitle?.takeIf { it.isNotBlank() } ?: eventSlug
    )
}

internal fun Preferences.readSnapshot(): EventWidgetSnapshot? =
    EventWidgetSnapshotSerializer.decode(this[SNAPSHOT_KEY])
