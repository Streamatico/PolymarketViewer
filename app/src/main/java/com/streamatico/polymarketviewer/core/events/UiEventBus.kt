package com.streamatico.polymarketviewer.core.events

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Resource-backed user-facing text.
 *
 * Keeping UI text resource-based here prevents lower layers from emitting
 * hardcoded, non-localized strings.
 */
data class UiText(
    @StringRes val resId: Int,
    val formatArgs: List<Any> = emptyList()
) {
    constructor(@StringRes resId: Int, vararg formatArgs: Any) : this(resId, formatArgs.toList())

    fun resolve(context: Context): String = context.getString(resId, *formatArgs.toTypedArray())
}

sealed class UiEvent {
    data class ShowSnackbar(val text: UiText) : UiEvent()
}

interface UiEventBus {
    val events: Flow<UiEvent>
    suspend fun emit(event: UiEvent)
}

/**
 * App-wide fire-and-forget UI event stream.
 *
 * The buffer keeps only the latest pending event so a new snackbar request can
 * replace an older one when the UI is busy processing a previous event.
 */
class DefaultUiEventBus : UiEventBus {
    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val events: Flow<UiEvent> = _events.asSharedFlow()

    override suspend fun emit(event: UiEvent) {
        _events.emit(event)
    }
}

