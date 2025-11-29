package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.TrendText
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks
import java.time.OffsetDateTime

@Composable
internal fun ClosedPositionItem(
    position: UserClosedPositionDto,
    onClick: () -> Unit
) {
    val cost = position.totalBought * position.avgPrice // 302.40$
    val profit      = position.realizedPnl // 154.23$
    val finalValue  = cost + profit // 456.63$
    val roiPercent  = (profit / cost * 100).toInt() // 51%

    ClosedPositionItemContent(
        totalBought = position.totalBought,
        avgPrice = position.avgPrice,
        outcome = position.outcome,
        closedAt = position.timestamp,

        profit = profit,
        finalValue = finalValue,
        roiPercent = roiPercent,

        eventIcon = position.icon,
        eventTitle = position.title,

        onClick = onClick,
    )
}

@Composable
private fun ClosedPositionItemContent(
    totalBought: Double,
    avgPrice: Double,

    outcome: String,
    closedAt: OffsetDateTime,

    profit: Double,
    finalValue: Double,
    roiPercent: Int,

    eventIcon: String?,
    eventTitle: String?,

    onClick: () -> Unit
) {
    val isWin = profit >= 0
    val pnlColor = if (isWin) ExtendedTheme.colors.onTrendUpContainer else ExtendedTheme.colors.onTrendDownContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Image + Title
            Row(verticalAlignment = Alignment.Top) {
                if (eventIcon != null) {
                    AsyncImage(
                        model = eventIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = eventTitle ?: "Unknown Market",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Content Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Outcome Badge + Shares
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Outcome Badge
                        TrendText(
                            isPositive = isWin,
                            text = if (isWin) "Won" else "Lost",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(Modifier.width(8.dp))

                        // Price (e.g. "57c")
                        val priceCentsText = UiFormatter.formatPriceCents(avgPrice)
                        Text(
                            text = "${UiFormatter.formatPositionSize(totalBought)} $outcome at $priceCentsText",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Closed on ${UiFormatter.formatDateOnly(closedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right: Value + PnL
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = UiFormatter.formatCurrency(finalValue),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    val sign = if (profit >= 0) "+" else ""
                    val pnlText = "${sign}${UiFormatter.formatCurrency(profit)}"
                    val percentText = " ($roiPercent%)"

                    Text(
                        text = pnlText + percentText,
                        style = MaterialTheme.typography.bodySmall,
                        color = pnlColor
                    )
                }
            }
        }
    }
}

// === Previews ===

@Preview
@Composable
private fun ClosedPositionItemPreview() {
    ClosedPositionItem(
        position = ProfilePreviewMocks.closedPositionsSample.first(),
        onClick = {}
    )
}