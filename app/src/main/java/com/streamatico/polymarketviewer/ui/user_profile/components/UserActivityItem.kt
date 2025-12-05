package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.ui.shared.ComposableUiFormatter
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.TrendText
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks

@Composable
internal fun UserActivityItem(
    userActivity: UserActivityDto,
    onClick: (() -> Unit)?
) {
    PositionCard(
        modifier = Modifier
            .fillMaxWidth()
            .let{
                if(onClick != null) {
                    it.clickable(onClick = onClick)
                } else {
                    it
                }
            }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            if (!userActivity.icon.isNullOrBlank()) {
                AsyncImage(
                    model = userActivity.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                val imageVector = when(userActivity.type) {
                    "REWARD" -> Icons.Outlined.CardGiftcard
                    "YIELD" -> Icons.Outlined.Savings
                    else -> null
                }

                if(imageVector != null) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterVertically),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            // Middle: Market Info
            Column(Modifier.weight(1f)) {
                MarketInfoSection(userActivity)
            }

            // Right: Amount + Time
            Column(
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

@Composable
private fun MarketInfoSection(userActivity: UserActivityDto) {
    val title = when(userActivity.type) {
        "REWARD" -> stringResource(R.string.activity_title_reward)
        "YIELD" -> stringResource(R.string.activity_title_yield)
        else -> userActivity.title.ifBlank { "???" }
    }

    Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(4.dp))

    FlowRow(
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        val textStyle = MaterialTheme.typography.bodyMedium

        when(userActivity.type) {
            "TRADE" -> {
                Text(
                    text = (userActivity.side?.lowercase()?.capitalize(Locale.current)?: "") + " ",
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
            }
            else -> {
                Text(
                    text = userActivity.type.lowercase().capitalize(Locale.current),
                    style = textStyle,
                )
            }
        }

        Text(
            text = " ${UiFormatter.formatPositionSize(userActivity.size)} shares",
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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

@Preview
@Composable
private fun YieldActivityItemPreview() {
    UserActivityItem(
        userActivity = ProfilePreviewMocks.sampleUserActivityYield,
        onClick = null
    )
}

@Preview
@Composable
private fun RewardActivityItemPreview() {
    UserActivityItem(
        userActivity = ProfilePreviewMocks.sampleUserActivityReward,
        onClick = null
    )
}