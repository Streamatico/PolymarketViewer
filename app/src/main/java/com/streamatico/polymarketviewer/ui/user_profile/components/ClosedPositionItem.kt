package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.TrendText
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks

@Composable
internal fun ClosedPositionItem(
    position: UserClosedPositionDto,
    onClick: () -> Unit
) {
    val cost = position.totalBought * position.avgPrice // 302.40$
    val profit      = position.realizedPnl // 154.23$
    val finalValue  = cost + profit // 456.63$
    val roiPercent  = profit / cost // 0.51 (51%)

    val isWin = profit >= 0

    PositionItemScaffold(
        icon = position.icon,
        title = position.title,
        positionContent = {
            // Outcome Badge
            TrendText(
                isPositive = isWin,
                text = stringResource(if (isWin) R.string.position_won else R.string.position_lost),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.width(8.dp))

            // Price (e.g. "57c")
            val priceCentsText = UiFormatter.formatPriceCents(position.avgPrice)
            Text(
                text = stringResource(
                    R.string.closed_position_summary_format,
                    UiFormatter.formatPositionSize(position.totalBought) ?: "0",
                    position.outcome,
                    priceCentsText
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },

        dateFooterText = stringResource(
            R.string.position_closed_on_format,
            UiFormatter.formatDateOnly(position.timestamp)
        ),
        positionValue = finalValue,
        pnl = profit,
        percentPnl = roiPercent,

        onClick = onClick
    )
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
