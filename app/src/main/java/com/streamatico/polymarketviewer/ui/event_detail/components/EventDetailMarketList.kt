package com.streamatico.polymarketviewer.ui.event_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolvedOutcome
import com.streamatico.polymarketviewer.data.model.gamma_api.getTitleOrDefault
import com.streamatico.polymarketviewer.data.model.gamma_api.getYesTitle
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice
import com.streamatico.polymarketviewer.ui.event_list.components.priceAlpha
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import kotlin.math.abs

private const val MARKET_DISPLAY_LIMIT = 5

fun LazyListScope.eventDetailMarketList(
    sortedMarkets: List<MarketDto>,
    isMarketListExpanded: Boolean,
    showMarketImages: Boolean,
    onMarketExpandToggle: () -> Unit,
    onMarketClick: (String) -> Unit
) {
    // Determine which markets to display
    val visibleMarkets = if (sortedMarkets.size > MARKET_DISPLAY_LIMIT && !isMarketListExpanded) {
        sortedMarkets.take(MARKET_DISPLAY_LIMIT)
    } else {
        sortedMarkets
    }

    val trendIndicatorEndPadding = visibleMarkets
        .any { x -> x.oneDayPriceChange != null && displayOneDayPriceChange(x.oneDayPriceChange) }
        .let {
            if(it) 40.dp else 0.dp
        }

    val hiddenMarketCount = sortedMarkets.size - MARKET_DISPLAY_LIMIT

    item {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(end = trendIndicatorEndPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outcome".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "% Chance".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            HorizontalDivider()
        }
    }

    val isSingleMarket = sortedMarkets.size == 1

    items(visibleMarkets, key = { it.id }) { market ->
        EventDetailMarketRow(
            market = market,
            isSingleMarket = isSingleMarket,
            showIcon = showMarketImages,
            trendIndicatorEndPadding = trendIndicatorEndPadding,
            onClick = { onMarketClick(market.id) }
        )
    }

    // Add "Show More/Less" button if needed
    if (sortedMarkets.size > MARKET_DISPLAY_LIMIT) {
        item {
            Text(
                text = if (isMarketListExpanded) "Show less" else "+ $hiddenMarketCount more outcomes",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onMarketExpandToggle() }
            )
        }
    }
}

@Composable
private fun EventDetailMarketRow(
    market: MarketDto,
    isSingleMarket: Boolean,
    showIcon: Boolean,
    trendIndicatorEndPadding: Dp,
    onClick: () -> Unit
) {
    val outcomeText = if (isSingleMarket) market.getYesTitle()
    else market.getTitleOrDefault(market.question)

    val iconUrl = market.iconUrl
    val resolutionStatus = market.getResolutionStatus()
    val price = market.yesPrice()
    val oneDayPriceChange = market.oneDayPriceChange
    val volume = market.volume


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Start Icon --- //
            if(showIcon && iconUrl != null) {
                AsyncImage(
                    model = iconUrl,
                    contentDescription = "$outcomeText icon",
                    modifier = Modifier
                        .size(40.dp) // Adjust size as needed
                        .clip(CircleShape) // Keep it circular? Or MaterialTheme.shapes.small?
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp)) // Space between icon and text
            }
            // --- End Icon --- //

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = outcomeText,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    volume?.let {
                        Text(
                            text = UiFormatter.formatLargeValueUsd(it, suffix = " Vol."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if(resolutionStatus != null) {
                        val resolutionText = when(resolutionStatus) {
                            MarketResolutionStatus.RESOLVED -> "(Resolved)"
                            MarketResolutionStatus.DISPUTED -> "(Disputed)"
                        }

                        Text(
                            text = resolutionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Chance
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isResolved = resolutionStatus == MarketResolutionStatus.RESOLVED

                    val chanceText: String = if (isResolved) {
                        market.getResolvedOutcome() ?: "??"
                    } else {
                        UiFormatter.formatPriceAsPercentage(price)
                    }

                    Text(
                        modifier = Modifier.priceAlpha(price, isResolved),
                        text = chanceText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Box(
                        Modifier.widthIn(min = trendIndicatorEndPadding)
                    ) {
                        if (!isResolved && oneDayPriceChange != null && displayOneDayPriceChange(oneDayPriceChange)) {
                            val priceChangePercent = (oneDayPriceChange * 100).toInt()
                            ChangePercentIndicator(
                                priceChangePercent,
                                Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        price?.let {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { it.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = trendIndicatorEndPadding)
            )
        }
    }
}

private fun displayOneDayPriceChange(oneDayPriceChange: Double): Boolean {
    return abs(oneDayPriceChange) >= 0.01
}

@Preview(showBackground = true)
@Composable
private fun EventMarketsListPreview() {
    LazyColumn {
        eventDetailMarketList(
            sortedMarkets = listOf(
                PreviewMocks.sampleMarket1,
                PreviewMocks.sampleMarket2,
                PreviewMocks.sampleMarket3,
                PreviewMocks.sampleMarket4LowPrice
            ),
            isMarketListExpanded = false,
            showMarketImages = true,
            onMarketExpandToggle = {},
            onMarketClick = {}
        )
    }
}

