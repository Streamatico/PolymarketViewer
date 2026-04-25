package com.streamatico.polymarketviewer.ui.shared

data class UiError(
    val title: String,
    val details: String? = null
) {
    val fullMessage: String
        get() = buildString {
            append(title)
            details
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    append('\n')
                    append(it)
                }
        }
}

