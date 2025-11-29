package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.ui.shared.ComposableUiFormatter
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.TrendText
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks

@Composable
internal fun UserActivityItem(
    userActivity: UserActivityDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxWidth(),
            //horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (userActivity.icon != null) {
                AsyncImage(
                    model = userActivity.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.small)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
                //Spacer(modifier = Modifier.width(12.dp))
            }
            // Middle: Market Info
            Column(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .weight(1f)
            ) {
                Text(
                    text = userActivity.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val textStyle = MaterialTheme.typography.bodyMedium

                    if(userActivity.type == "TRADE") {
                        Text(
                            text = (userActivity.side?.lowercase()?.capitalize(Locale.current)
                                ?: "") + " ",
                            style = textStyle,
                        )

                        val positionText =
                            "${userActivity.outcome} ${UiFormatter.formatPriceCents(userActivity.price)}"

                        TrendText(
                            isPositive = userActivity.outcomeIndex == 0,
                            text = positionText,
                            style = textStyle,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = userActivity.type.lowercase().capitalize(Locale.current),
                            style = textStyle,
                        )
                    }

                    Text(
                        text = " ${UiFormatter.formatPositionSize(userActivity.size)} shares",
                        style = textStyle,
                    )
                }

            }

            // Right: Amount + Time
            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = UiFormatter.formatCurrency(userActivity.value),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val relativeTime = ComposableUiFormatter.formatRelativeTime(userActivity.timestamp)
                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==== Previews ====

@Preview
@Composable
private fun TradeActivityItemPreview() {
    UserActivityItem(
        userActivity = ProfilePreviewMocks.sampleUserActivityTrade,
        onClick = {}
    )
}

@Preview
@Composable
private fun RedeemActivityItemPreview() {
    UserActivityItem(
        userActivity = ProfilePreviewMocks.sampleUserActivityRedeem,
        onClick = {}
    )
}