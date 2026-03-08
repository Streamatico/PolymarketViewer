package com.streamatico.polymarketviewer.data.analytics

sealed class AnalyticsEvent(
    val wireName: String,
    val includeFirstLaunchFlag: Boolean = false
) {
    data object AppLaunched : AnalyticsEvent(
        wireName = "app-launched",
        includeFirstLaunchFlag = true
    )

    data object WidgetCreated : AnalyticsEvent("widget-created")
}
