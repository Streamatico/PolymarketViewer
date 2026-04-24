package com.streamatico.polymarketviewer.ui.shared.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.streamatico.polymarketviewer.core.events.UiEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun GlobalSnackbarHost(
    uiEvents: Flow<UiEvent>,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val context = LocalContext.current

    LaunchedEffect(uiEvents, snackbarHostState) {
        uiEvents.collectLatest { event ->
            if (event is UiEvent.ShowSnackbar) {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message = event.text.resolve(context))
            }
        }
    }

    SnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState
    )
}

