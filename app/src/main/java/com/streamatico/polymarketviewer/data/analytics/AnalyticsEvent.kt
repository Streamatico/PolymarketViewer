package com.streamatico.polymarketviewer.data.analytics

sealed class AnalyticsEvent(
    val wireName: String
) {
    data object AppLaunched : AnalyticsEvent("app-launched")
    data object WidgetCreated : AnalyticsEvent("widget-created")
}
