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
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.TrendText
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks

@Composable
internal fun PositionItem(
    position: UserPositionDto,
    onClick: () -> Unit
) {
    val isWin = (position.pnl ?: 0.0) >= 0
    val pnlColor = if (isWin) ExtendedTheme.colors.onTrendUpContainer else ExtendedTheme.colors.onTrendDownContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Image + Title
            Row(verticalAlignment = Alignment.Top) {
                if (position.icon != null) {
                    AsyncImage(
                        model = position.icon,
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
                    text = position.title ?: "Unknown Market",
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
                            isPositive = position.outcome == "Yes",
                            text = position.outcome ?: "?",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "${UiFormatter.formatPositionSize(position.size) ?: "0"} shares at",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Price (e.g. "57c")
                        Text(
                            text = " ${UiFormatter.formatPriceCents(position.price)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if(position.endDate != null) {
                        Text(
                            text = "Ends on ${position.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: Value + PnL
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = UiFormatter.formatCurrency(position.value),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    val pnl = position.pnl
                    val percentPnl = position.percentPnl
                    if (pnl != null) {
                        val sign = if (pnl >= 0) "+" else ""
                        val pnlText = "${sign}${UiFormatter.formatCurrency(pnl)}"
                        val percentText = percentPnl?.let { " (${(it * 100).toInt()}%)" } ?: ""

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
}

// === Previews ===

@Preview
@Composable
private fun PositionItemPreview() {
    PositionItem(
        position = ProfilePreviewMocks.positionsSample.first(),
        onClick = {}
    )
}