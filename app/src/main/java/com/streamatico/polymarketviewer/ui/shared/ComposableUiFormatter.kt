package com.streamatico.polymarketviewer.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

// Add import for Duration
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object ComposableUiFormatter {
    @Composable
    fun formatRelativeTime(dateTime: OffsetDateTime): String {
        val now = OffsetDateTime.now(dateTime.offset)
        val duration = ChronoUnit.SECONDS.between(dateTime, now)

        return when {
            duration < 60 -> "${duration}s ago"
            duration < 3600 -> "${duration / 60}m ago"
            duration < 86400 -> "${duration / 3600}h ago"
            duration < 604800 -> "${duration / 86400}d ago"
            else -> {
                val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()) }
                dateTime.format(formatter)
            }
        }
    }

    /**
     * Formats the remaining time until the given dateTime or the date itself.
     * Returns "Ended ... ago" if the date is in the past.
     * Returns "?" if the dateTime is null.
     * Returns a Pair: the formatted String and a Boolean indicating if the event has ended.
     */
    @Composable
    fun formatTimeRemainingOrDate(dateTime: OffsetDateTime?): Pair<String, Boolean> {
        if (dateTime == null) return "?" to false

        val now = OffsetDateTime.now(dateTime.offset)
        val durationSeconds = ChronoUnit.SECONDS.between(now, dateTime)
        val hasEnded = durationSeconds < 0
        val duration: Duration = durationSeconds.seconds

        val shortDateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()) }
        val longDateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()) }

        return when {
            // Event ended
            hasEnded -> "Ended ${formatRelativeTime(dateTime)}" to true

            // Event ends very soon
            duration < 1.minutes -> "Ends in <1m" to false
            duration < 1.hours -> "Ends in ${duration.inWholeMinutes}m" to false
            duration < 24.hours -> "Ends in ${duration.inWholeHours}h" to false
            duration < 7.days -> "Ends in ${duration.inWholeDays}d" to false

            // Event ends later this year
            dateTime.year == now.year -> "Ends ${dateTime.format(shortDateFormatter)}" to false

            // Event ends in a future year
            else -> "Ends ${dateTime.format(longDateFormatter)}" to false
        }
    }
}