package com.streamatico.polymarketviewer.ui.shared

import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.absoluteValue

internal object UiFormatter {
    fun formatDateTimeLong(dateTime: OffsetDateTime): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        return dateTime.format(formatter)
    }

    // --- Helper function for formatting position size --- //
    fun formatPositionSize(size: String?): String? {
        if (size == null) return null
        return try {
            val number = BigDecimal(size)
            // Assuming the size represents value in smallest units (e.g., cents/wei)
            // Adjust divisor based on actual currency/token decimals (e.g., 10^6 for USDC, 10^18 for ETH)
            // Using 10^6 (USDC) as a guess
            val divisor = BigDecimal("1000000")
            val value = number.divide(divisor)

            when {
                value >= BigDecimal(1_000_000) -> String.format(Locale.US, "%.1fM", value.divide(BigDecimal(1_000_000)))
                value >= BigDecimal(1_000) -> String.format(Locale.US, "%.1fK", value.divide(BigDecimal(1_000)))
                else -> String.format(Locale.US, "%.0f", value) // Show whole number if less than 1K
            }
        } catch (e: Exception) {
            Log.w("Formatting", "Failed to format position size: $size", e)
            null // Return null on error
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
}