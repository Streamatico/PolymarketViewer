package com.streamatico.polymarketviewer.ui.shared

import android.content.Context
import android.util.Log
import androidx.core.os.ConfigurationCompat
import com.streamatico.polymarketviewer.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

internal object UiFormatter {
    private val MINUTE_IN_SECONDS = Duration.ofMinutes(1).seconds
    private val HOUR_IN_SECONDS = Duration.ofHours(1).seconds
    private val DAY_IN_SECONDS = Duration.ofDays(1).seconds
    private val WEEK_IN_SECONDS = Duration.ofDays(7).seconds

    fun formatDateTimeLong(dateTime: OffsetDateTime): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        return dateTime.format(formatter)
    }

    // Format like: 12 Oct 2024
    fun formatDateOnly(dateTime: OffsetDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return dateTime.format(formatter)
    }

    fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }

    // --- Helper function for formatting position size --- //
    fun formatPositionSize(size: String?): String? {
        if (size == null) return null
        return try {
            val number = BigDecimal(size)
            return internalFormatPositionSize(number)
        } catch (e: Exception) {
            Log.w("Formatting", "Failed to format position size: $size", e)
            null // Return null on error
        }
    }

    fun formatPositionSize(size: Double?): String? {
        if (size == null) return null
        return String.format(Locale.getDefault(), "%.1f", size)
    }

    fun formatPriceCents(price: Double): String {
        val priceCents = (price * 100).toInt()
        return "${priceCents}¢"
    }

    private fun internalFormatPositionSize(number: BigDecimal): String {
        // Assuming the size represents value in smallest units (e.g., cents/wei)
        // Adjust divisor based on actual currency/token decimals (e.g., 10^6 for USDC, 10^18 for ETH)
        // Using 10^6 (USDC) as a guess
        val divisor = BigDecimal("1000000")
        val value = number.divide(divisor)

        return when {
            value >= BigDecimal(1_000_000) -> String.format(Locale.US, "%.1fM", value.divide(BigDecimal(1_000_000)))
            value >= BigDecimal(1_000) -> String.format(Locale.US, "%.1fK", value.divide(BigDecimal(1_000)))
            else -> String.format(Locale.US, "%.0f", value) // Show whole number if less than 1K
        }
    }

    fun isPriceLow1Percent(price: Double?): Boolean {
        if(price == null) return false
        return price > 0 && price < 0.005
    }

    fun formatPriceAsPercentage(price: Double?): String {
        if (price == null) return "- " // Space instead of percentage sign

        if(isPriceLow1Percent(price)) {
            return "<1%"
        }

        val format = NumberFormat.getPercentInstance(Locale.getDefault())
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        format.roundingMode = RoundingMode.HALF_UP
        val result = format.format(price)

        return result
    }

    // Function for formatting large numeric values (Volume, Liquidity)
    private fun formatLargeValue(value: Double): String {
        return when {
            value.absoluteValue >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", value / 1_000_000_000)
            value.absoluteValue >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000)
            value.absoluteValue >= 1_000 -> String.format(Locale.US, "%.0fK", value / 1_000)
            else -> String.format(Locale.US, "%.2f", value)
        }.replace(",", " ")
    }

    fun formatLargeValueUsd(value: Double, suffix: String = ""): String {
        return "$${formatLargeValue(value)}$suffix"
    }

    fun getMarketBadgeText(context: Context, badgeState: MarketBadgeState): String {
        val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.getDefault()

        return when (badgeState) {
            MarketBadgeState.Resolved -> context.getString(R.string.market_badge_resolved)
            MarketBadgeState.Locked -> context.getString(R.string.market_badge_locked)
            MarketBadgeState.Resolving -> context.getString(R.string.market_badge_resolving)
            is MarketBadgeState.ActiveEndsOnDate -> {
                formatTimeRemainingOrDate(
                    context = context,
                    dateTime = badgeState.endDate,
                    locale = locale
                )
            }
            MarketBadgeState.Unknown -> context.getString(R.string.market_badge_unknown)
        }
    }

    fun formatRelativePastTime(
        dateTime: OffsetDateTime,
        now: OffsetDateTime = OffsetDateTime.now(dateTime.offset),
        locale: Locale = Locale.getDefault()
    ): String {
        val durationSeconds = ChronoUnit.SECONDS.between(dateTime, now)

        return when {
            durationSeconds < 0 -> "0s ago"
            durationSeconds < MINUTE_IN_SECONDS -> "${durationSeconds}s ago"
            durationSeconds < HOUR_IN_SECONDS -> "${durationSeconds / MINUTE_IN_SECONDS}m ago"
            durationSeconds < DAY_IN_SECONDS -> "${durationSeconds / HOUR_IN_SECONDS}h ago"
            durationSeconds < WEEK_IN_SECONDS -> "${durationSeconds / DAY_IN_SECONDS}d ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)
                dateTime.format(formatter)
            }
        }
    }

    private fun formatTimeRemainingOrDate(
        context: Context,
        dateTime: OffsetDateTime?,
        now: OffsetDateTime? = null,
        locale: Locale = Locale.getDefault()
    ): String {
        if (dateTime == null) return context.getString(R.string.market_badge_unknown)

        val currentTime = now ?: OffsetDateTime.now(dateTime.offset)
        val durationSeconds = ChronoUnit.SECONDS.between(currentTime, dateTime)
        if (durationSeconds < 0) return context.getString(R.string.market_badge_resolving)

        val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d", locale)
        val longDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", locale)

        return when {
            durationSeconds < MINUTE_IN_SECONDS -> context.getString(R.string.market_badge_ends_in_less_than_minute)
            durationSeconds < HOUR_IN_SECONDS -> context.getString(
                R.string.market_badge_ends_in_minutes,
                durationSeconds / MINUTE_IN_SECONDS
            )
            durationSeconds < DAY_IN_SECONDS -> context.getString(
                R.string.market_badge_ends_in_hours,
                durationSeconds / HOUR_IN_SECONDS
            )
            durationSeconds < WEEK_IN_SECONDS -> context.getString(
                R.string.market_badge_ends_in_days,
                durationSeconds / DAY_IN_SECONDS
            )
            dateTime.year == currentTime.year -> context.getString(
                R.string.market_badge_ends_on_date,
                dateTime.format(shortDateFormatter)
            )
            else -> context.getString(
                R.string.market_badge_ends_on_date,
                dateTime.format(longDateFormatter)
            )
        }
    }
}