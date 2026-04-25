package com.streamatico.polymarketviewer.ui.shared

private object UiErrorMapper {
    fun fromThrowable(
        throwable: Throwable,
        title: String,
    ): UiError {
        val rawMessage = throwable.message?.trim().orEmpty()
        val causeMessage = throwable.cause?.message?.trim().orEmpty()

        val details = when {
            rawMessage.isBlank() && causeMessage.isBlank() -> "Unknown error (${throwable::class.java.simpleName})"
            rawMessage.isBlank() -> causeMessage
            rawMessage.isLikeHostOrUrlOnly() -> buildString {
                append("Unable to connect to ")
                append(rawMessage)
                if (causeMessage.isNotBlank() && !causeMessage.equals(rawMessage, ignoreCase = true)) {
                    append(". ")
                    append(causeMessage)
                }
            }

            else -> rawMessage
        }

        return UiError(
            title = title,
            details = details
        )
    }
}

fun Throwable.toUiError(
    title: String,
): UiError = UiErrorMapper.fromThrowable(this, title)

private fun String.isLikeHostOrUrlOnly(): Boolean {
    return !this.contains(' ') && contains('.')
}

