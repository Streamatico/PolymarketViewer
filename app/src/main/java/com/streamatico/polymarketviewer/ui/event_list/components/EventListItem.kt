package com.streamatico.polymarketviewer.ui.event_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseEventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.BaseMarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventType
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolutionStatus
import com.streamatico.polymarketviewer.data.model.gamma_api.getResolvedOutcome
import com.streamatico.polymarketviewer.data.model.gamma_api.getTitleOrDefault
import com.streamatico.polymarketviewer.data.model.gamma_api.getYesTitle
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice
import com.streamatico.polymarketviewer.ui.shared.ComposableUiFormatter
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.sortedByViewPriority
import com.streamatico.polymarketviewer.ui.shared.sortedForShortView
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

// Constant for maximum number of visible markets
private const val MAX_VISIBLE_MARKETS = 3

/**
 * Composable for displaying one Event (Event) and its nested Markets.
 */
@Composable
fun EventListItem(
    event: BaseEventDto,
    isInWatchlist: Boolean = false,
    onToggleWatchlist: () -> Unit = {},
    onClick: () -> Unit // New parameter for clicking on the entire card
) {
    // State to track whether the market/outcome list is expanded
    var isMarketListExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .alpha(if (event.closed) 0.7f else 1f)
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { onClick() }, // Click on the entire card
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column {
                // --- Header (Image + Title) --- //
                Row(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = 48.dp, // Add padding for bookmark icon
                        bottom = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.imageUrl != null) {
                        AsyncImage(
                            model = event.imageUrl,
                            contentDescription = event.title,
                            modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // --- Market Content (choose display style) --- //
                when (event.eventType) {
                    EventType.BinaryEvent -> {
                        BinaryMarketContent(market = event.baseMarkets.first())
                    }

                    EventType.CategoricalMarket -> {
                        val mainMarketByThreshold = event.baseMarkets
                            .firstOrNull { it.groupItemThreshold == 0 }

                        CategoricalMarketContent(
                            market = mainMarketByThreshold ?: event.baseMarkets.first(),
                            isExpanded = isMarketListExpanded, // Pass state
                            onToggleExpand = {
                                isMarketListExpanded = !isMarketListExpanded
                            } // Pass lambda
                        )
                    }

                    EventType.MultiMarket -> {
                        val sortedMarkets = remember(event, isMarketListExpanded) {
                            if (isMarketListExpanded || event.baseMarkets.size <= (MAX_VISIBLE_MARKETS + 1)) {
                                event.baseMarkets.sortedByViewPriority(event.sortByEnum)
                            } else {
                                event.baseMarkets
                                    .sortedForShortView(MAX_VISIBLE_MARKETS)
                            }
                        }

                        MultiMarketContent(
                            markets = sortedMarkets,
                            totalMarketsSize = event.baseMarkets.size,
                            isExpanded = isMarketListExpanded, // Pass state
                            onToggleExpand = {
                                isMarketListExpanded = !isMarketListExpanded
                            }
                        )
                    }
                }

                // --- Footer (Volume + End Date) --- //
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 12.dp,
                            top = 4.dp
                        ), // Adjusted padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Volume on the left
                    event.volume?.let {
                        Text(
                            text = UiFormatter.formatLargeValueUsd(it, suffix = stringResource(R.string.volume_suffix)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Push end date to the right

                    // End Date/Time Remaining on the right
                    val (endDateText, hasEnded) = ComposableUiFormatter.formatTimeRemainingOrDate(
                        event.endDate
                    )
                    Text(
                        text = endDateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasEnded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.End // Align text to the end
                    )
                }
            }

            // Watchlist Button
            IconButton(
                onClick = onToggleWatchlist,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = if (isInWatchlist) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = stringResource(if (isInWatchlist) R.string.cd_remove_from_watchlist else R.string.cd_add_to_watchlist),
                    tint = if (isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Composable for "Binary market" style --- //
@Composable
private fun BinaryMarketContent(
    market: BaseMarketDto
) {
    val yesPrice = market.yesPrice()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutcomeTextRow(
                title = market.getYesTitle(),
                marketResolutionStatus = market.getResolutionStatus(),
                modifier = Modifier.weight(1f) // Takes remaining space
            )
            yesPrice?.let {
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = UiFormatter.formatPriceAsPercentage(it),
                    style = MaterialTheme.typography.headlineSmall, // Larger
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        // Add ProgressIndicator
        yesPrice?.let {
             Spacer(modifier = Modifier.height(8.dp))
             LinearProgressIndicator(
                 progress = { it.toFloat() }, // Use lambda for deferred reading
                 modifier = Modifier.fillMaxWidth()
             )
        }
    }
}

// --- Composable for SINGLE CATEGORICAL market --- //
@Composable
private fun CategoricalMarketContent(
    market: BaseMarketDto,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val outcomes = market.outcomes
    val prices = market.outcomePrices

    // Create (Outcome, Price) pairs for sorting and display
    val outcomePricePairs = remember(outcomes, prices) {
        outcomes.zip(prices) { outcome, price -> outcome to price }
            //.sortedByDescending { it.second } // Sort by descending price
    }

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        // Show all if expanded or if there are exactly 4 outcomes
        val visibleOutcomes = if (isExpanded || outcomePricePairs.size == MAX_VISIBLE_MARKETS + 1) {
            outcomePricePairs
        } else {
            outcomePricePairs.take(MAX_VISIBLE_MARKETS)
        }

        visibleOutcomes.forEach { (outcome, price) ->
            // Use universal MarketRow
            MarketRow(title = outcome, price = price, marketResolutionStatus = market.getResolutionStatus())
        }

        // Show button only if there are more than 4 outcomes (more than 1 hidden)
        if (outcomePricePairs.size > MAX_VISIBLE_MARKETS + 1) {
            // Display "+ X more outcomes"
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                text = if (isExpanded) stringResource(R.string.action_show_less) else {
                    val count = outcomePricePairs.size - MAX_VISIBLE_MARKETS
                    pluralStringResource(R.plurals.more_outcomes_count, count, count)
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary, // Make color clickable
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleExpand() } // Make clickable
             )
        }
    }
}

// --- Composable for MULTIPLE BINARY markets (elections) --- //
@Composable
private fun MultiMarketContent(
    markets: List<BaseMarketDto>,
    totalMarketsSize: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    if(markets.isEmpty()){
        Text(stringResource(R.string.error_no_market_data), Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        markets.forEach { market ->
            val price = market.yesPrice()

            // Use groupItemTitle or question for text
            val outcomeText = if(markets.size == 1) market.getYesTitle()
                else market.getTitleOrDefault(market.question)
            // Use universal MarketRow
            MarketRow(
                title = outcomeText,
                price = price,
                marketResolutionStatus = market.getResolutionStatus(),
                resolvedOutcome = market.getResolvedOutcome()
            )
        }

        // Show button only if there are more than 4 markets (more than 1 hidden)
        if (isExpanded || markets.size < totalMarketsSize) {
            val moreText: String
            if(isExpanded) {
                moreText = stringResource(R.string.action_show_less)
            } else {
                val remainingCount = totalMarketsSize - markets.size
                moreText = pluralStringResource(R.plurals.more_outcomes_count, remainingCount, remainingCount)
            }

            // Display "+ X more markets"
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                text = moreText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary, // Make color clickable
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onToggleExpand() } // Make clickable
             )
        }
    }
}

// --- MarketRow (universal row for outcome) --- //
// Displays a row for one outcome with its name and price.
@Composable
private fun MarketRow(
    title: String,
    price: Double?,
    marketResolutionStatus: MarketResolutionStatus?,
    resolvedOutcome: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isResolved = marketResolutionStatus == MarketResolutionStatus.RESOLVED

        OutcomeTextRow(
            modifier = Modifier.weight(1f),
            title = title,
            marketResolutionStatus = marketResolutionStatus,
        )
        Spacer(modifier = Modifier.width(8.dp))

        val chanceText: String = if (isResolved && resolvedOutcome != null) {
            resolvedOutcome
        } else {
            UiFormatter.formatPriceAsPercentage(price)
        }

        // Chance
        Text(
            modifier = Modifier.priceAlpha(price, isResolved),
            text = chanceText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

internal fun Modifier.priceAlpha(price: Double?, isResolved: Boolean): Modifier {
    if (!isResolved && UiFormatter.isPriceLow1Percent(price)) return this.alpha(0.6f)
    return this
}

@Composable
private fun OutcomeTextRow(
    title: String,
    marketResolutionStatus: MarketResolutionStatus?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, // Show passed outcome text
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        marketResolutionStatus?.let {
            Spacer(modifier = Modifier.width(8.dp))

            val resolutionText = when (marketResolutionStatus) {
                MarketResolutionStatus.DISPUTED -> stringResource(R.string.status_disputed)
                MarketResolutionStatus.RESOLVED -> stringResource(R.string.status_resolved)
            }

            Text(
                text = resolutionText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// --- Previews --- //
@Preview(showBackground = true, name = "Binary Event Preview")
@Composable
private fun BinaryEventListItemPreview() {
    PolymarketAppTheme {
        EventListItem(event = PreviewMocks.sampleBinaryEvent, onClick = {})
    }
}

@Preview(showBackground = true, name = "Multi Market Event Preview (Corrected)")
@Composable
private fun MultiMarketEventListItemPreview() {
    PolymarketAppTheme {
        EventListItem(event = PreviewMocks.sampleMultiMarketOptimizedEvent, onClick = {})
    }
}

// NEW Preview for categorical market
@Preview(showBackground = true, name = "Categorical Event Preview")
@Composable
private fun CategoricalEventListItemPreview() {
    PolymarketAppTheme {
        EventListItem(event = PreviewMocks.sampleCategoricalEvent, onClick = {})
    }
}
