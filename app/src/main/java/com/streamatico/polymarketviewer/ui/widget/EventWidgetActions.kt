package com.streamatico.polymarketviewer.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import com.streamatico.polymarketviewer.MainActivity

internal object EventWidgetActions {
    fun openEventAction(context: Context, eventSlug: String): Action {
        return actionStartActivity(createOpenEventIntent(context, eventSlug = eventSlug))
    }

    fun refreshWidgetAction(): Action {
        return actionRunCallback<EventWidgetRefreshAction>()
    }

    private fun createOpenEventIntent(context: Context, eventSlug: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_EVENT_SLUG, eventSlug)
        }
}

// !!! The class must be global and not private
internal class EventWidgetRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        EventWidgetRefresher.refresh(context.applicationContext, glanceId)
    }
}
