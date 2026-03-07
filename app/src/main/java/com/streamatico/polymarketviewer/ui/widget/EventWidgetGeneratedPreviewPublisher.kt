package com.streamatico.polymarketviewer.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.streamatico.polymarketviewer.R

/**
 * Publishes runtime-generated widget previews on platforms that expose the API.
 * Falls back silently when the API is unavailable.
 */
internal object EventWidgetGeneratedPreviewPublisher {
    private const val TAG = "EventWidgetPreview"

    fun publishIfSupported(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, EventWidgetReceiver::class.java)
        val preview = RemoteViews(context.packageName, R.layout.widget_generated_preview)

        val published = runCatching {
            val threeArg = AppWidgetManager::class.java.getMethod(
                "setWidgetPreview",
                ComponentName::class.java,
                Int::class.javaPrimitiveType,
                RemoteViews::class.java
            )
            threeArg.invoke(
                appWidgetManager,
                provider,
                AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                preview
            )
            true
        }.getOrElse {
            runCatching {
                val fourArg = AppWidgetManager::class.java.getMethod(
                    "setWidgetPreview",
                    ComponentName::class.java,
                    Int::class.javaPrimitiveType,
                    RemoteViews::class.java,
                    Int::class.javaPrimitiveType
                )
                fourArg.invoke(
                    appWidgetManager,
                    provider,
                    AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN,
                    preview,
                    0
                )
                true
            }.getOrDefault(false)
        }

        if (!published) {
            Log.d(TAG, "setWidgetPreview API not available on this build; using previewLayout fallback")
        }
    }
}
