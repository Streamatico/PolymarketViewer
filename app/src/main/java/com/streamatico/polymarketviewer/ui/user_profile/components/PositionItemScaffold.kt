package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.theme.ExtendedTheme

@Composable
internal fun PositionItemScaffold(
    icon: String?,
    title: String?,

    positionContent: @Composable FlowRowScope.() -> Unit,
    dateFooterText: String?,

    positionValue: Double,
    pnl: Double?,
    percentPnl: Double?,

    onClick: () -> Unit
) {
    val isWin = (pnl ?: 0.0) >= 0
    val pnlColor = if (isWin) ExtendedTheme.colors.onTrendUpContainer else ExtendedTheme.colors.onTrendDownContainer

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header: Image + Title
            Row(verticalAlignment = Alignment.Top) {
                if (icon != null) {
                    AsyncImage(
                        model = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    modifier = Modifier.weight(1f),
                    text = title ?: "Unknown Market",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Content Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Outcome Badge + Shares
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FlowRow(
                        itemVerticalAlignment = Alignment.CenterVertically,
                        content = positionContent,
                    )

                    if(dateFooterText != null) {
                        Text(
                            text = dateFooterText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                // Right: Value + PnL
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = UiFormatter.formatCurrency(positionValue),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (pnl != null) {
                        val sign = if (pnl >= 0) "+" else ""
                        val pnlText = "${sign}${UiFormatter.formatCurrency(pnl)}"
                        val percentText = percentPnl?.let { " (${it.toInt()}%)" } ?: ""

                        Text(
                            text = pnlText + percentText,
                            textAlign = TextAlign.End,
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
    PositionItemScaffold(
        icon = "https://mock-url",
        title = "Event title",
        positionContent = {
            Text(text = "Mock Outcome")
        },

        dateFooterText = "Footer with date",
        positionValue = 12345.67,
        pnl = 123.45,
        percentPnl = 12345.678,

        onClick = {}
    )
}