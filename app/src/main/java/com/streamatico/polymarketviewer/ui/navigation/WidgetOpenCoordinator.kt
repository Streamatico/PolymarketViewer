package com.streamatico.polymarketviewer.ui.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal data class OpenEventCommand(
    val eventSlug: String,
    val requestId: Long
)

internal class WidgetOpenCoordinator {
    private val _openEventCommand = MutableStateFlow<OpenEventCommand?>(null)
    val openEventCommand: StateFlow<OpenEventCommand?> = _openEventCommand

    private var currentTopEventSlug: String? = null
    private var nextRequestId = 0L

    fun onWidgetIntentSlug(eventSlug: String?) {
        val slug = eventSlug ?: return
        if (currentTopEventSlug == slug) return

        _openEventCommand.value = createOpenEventCommand(slug)
    }

    fun onTopEventSlugChanged(eventSlug: String?) {
        currentTopEventSlug = eventSlug
    }

    private fun createOpenEventCommand(eventSlug: String): OpenEventCommand {
        nextRequestId += 1
        return OpenEventCommand(eventSlug = eventSlug, requestId = nextRequestId)
    }
}

