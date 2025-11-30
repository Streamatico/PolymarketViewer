package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.TrendText
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks

@Composable
internal fun PositionItem(
    position: UserPositionDto,
    onClick: () -> Unit
) {
    PositionItemScaffold(
        icon = position.icon,
        title = position.title,
        positionContent = {
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
        },
        dateFooterText = position.endDate?.let { "Ends on $it" },
        positionValue = position.value,
        pnl = position.pnl,
        percentPnl = position.percentPnl,

        onClick = onClick
    )
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