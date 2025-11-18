package com.streamatico.polymarketviewer.ui.event_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.BaseEventDto
import com.streamatico.polymarketviewer.data.model.BaseMarketDto
import com.streamatico.polymarketviewer.data.model.EventType
import com.streamatico.polymarketviewer.data.model.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.demoEventDto
import com.streamatico.polymarketviewer.data.model.demoMarketDto
import com.streamatico.polymarketviewer.data.model.demoOptimizedEventDto
import com.streamatico.polymarketviewer.data.model.demoOptimizedMarketDto
import com.streamatico.polymarketviewer.data.model.getResolutionStatus
import com.streamatico.polymarketviewer.data.model.getTitleOrDefault
import com.streamatico.polymarketviewer.data.model.getYesTitle
import com.streamatico.polymarketviewer.data.model.yesPrice
import com.streamatico.polymarketviewer.ui.shared.ComposableUiFormatter
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.sortedByViewPriority
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import java.time.OffsetDateTime

// Constant for maximum number of visible markets
private const val MAX_VISIBLE_MARKETS = 3

/**
 * Composable for displaying one Event (Event) and its nested Markets.
 */
@Composable
fun EventListItem(
    event: BaseEventDto,
    onEventClick: (String) -> Unit // New parameter for clicking on the entire card
) {
    // State to track whether the market/outcome list is expanded
    var isMarketListExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .alpha(if (event.closed) 0.7f else 1f)
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { onEventClick(event.id) }, // Click on the entire card
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
            Column {
                // --- Header (Image + Title) --- //
                Row(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = 16.dp,
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
                        CategoricalMarketContent(
                            market = event.baseMarkets.first(),
                            isExpanded = isMarketListExpanded, // Pass state
                            onToggleExpand = {
                                isMarketListExpanded = !isMarketListExpanded
                            } // Pass lambda
                        )
                    }

                    EventType.MultiMarket -> {
                        val sortedMarkets = remember(event) {
                            event.baseMarkets
                                .sortedByViewPriority(event.sortByEnum)
                        }

                        MultiMarketContent(
                            sortedMarkets = sortedMarkets,
                            isExpanded = isMarketListExpanded, // Pass state
                            onToggleExpand = {
                                isMarketListExpanded = !isMarketListExpanded
                            } // Pass lambda
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
                            text = UiFormatter.formatLargeValueUsd(it, suffix = " Vol."),
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
                outcomeText = market.getYesTitle(),
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
            .sortedByDescending { it.second } // Sort by descending price
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
            MarketRow(outcomeText = outcome, price = price, marketResolutionStatus = market.getResolutionStatus())
        }

        // Show button only if there are more than 4 outcomes (more than 1 hidden)
        if (outcomePricePairs.size > MAX_VISIBLE_MARKETS + 1) {
            // Display "+ X more outcomes"
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                text = if (isExpanded) "Show less" else "+ ${outcomePricePairs.size - MAX_VISIBLE_MARKETS} more ${getOutcomeDeclension(outcomePricePairs.size - MAX_VISIBLE_MARKETS)}",
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
    sortedMarkets: List<BaseMarketDto>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    if(sortedMarkets.isEmpty()){
        Text("No market data available", Modifier.padding(16.dp))
        return
    }

    // Create (Market, "Yes" Price) pairs for sorting
    val marketPricePairs = remember(sortedMarkets) {
        sortedMarkets
            .mapNotNull { market ->
                val yesPrice = market.yesPrice()
                if (yesPrice != null) market to yesPrice else null // Include only if Yes price exists
            }
    }

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        // Show all if expanded or if there are exactly 4 markets
        val visibleMarkets = if (isExpanded || marketPricePairs.size == MAX_VISIBLE_MARKETS + 1) {
            marketPricePairs
        } else {
            marketPricePairs.take(MAX_VISIBLE_MARKETS)
        }

        val remainingCount = marketPricePairs.size - MAX_VISIBLE_MARKETS

        visibleMarkets.forEach { (market, price) ->
            // Use groupItemTitle or question for text
            val outcomeText = if(sortedMarkets.size == 1) market.getYesTitle()
                else market.getTitleOrDefault(market.question)
            // Use universal MarketRow
            MarketRow(outcomeText = outcomeText, price = price, marketResolutionStatus = market.getResolutionStatus())
        }

        // Show button only if there are more than 4 markets (more than 1 hidden)
        if (marketPricePairs.size > MAX_VISIBLE_MARKETS + 1) {
            // Display "+ X more markets"
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                text = if (isExpanded) "Show less" else "+ $remainingCount more ${getOutcomeDeclension(remainingCount)}",
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

// NEW Helper function for outcome word declension
private fun getOutcomeDeclension(count: Int): String {
    return when {
        count == 1 -> "outcome"
        else -> "outcomes"
    }
}

// --- MarketRow (universal row for outcome) --- //
// Displays a row for one outcome with its name and price.
@Composable
private fun MarketRow(
    outcomeText: String,
    price: Double?,
    marketResolutionStatus: MarketResolutionStatus?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutcomeTextRow(
            modifier = Modifier.weight(1f),
            outcomeText = outcomeText,
            marketResolutionStatus = marketResolutionStatus,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = UiFormatter.formatPriceAsPercentage(price), // Show passed price
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OutcomeTextRow(
    outcomeText: String,
    marketResolutionStatus: MarketResolutionStatus?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = outcomeText, // Show passed outcome text
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        marketResolutionStatus?.let {
            Spacer(modifier = Modifier.width(8.dp))

            val resolutionText = when (marketResolutionStatus) {
                MarketResolutionStatus.DISPUTED -> "(Disputed)"
                MarketResolutionStatus.RESOLVED -> "(Resolved)"
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
    val previewMarket = demoMarketDto(
        id = "m1",
        question = "Will binary preview work?",
        slug = "m1-slug",
        description = "Binary test description",
        active = true,
        closed = false,
        resolutionSource = "Source B1",
        startDate = null,
        endDate = null,
        volume = 1000.0,

        liquidity = 50.0,
        outcomesJson = "[\"Yes\", \"No\"]",
        outcomePricesJson = "[\"0.65\", \"0.35\"]",
        groupItemTitle = "Binary Test",

        umaResolutionStatus = "disputed" // Example status
    )
    val previewEvent = demoEventDto(
        id = "event-bin",
        title = "Binary Event Example",
        slug = "event-bin-slug",
        description = "Event description for binary",
        category = "Test",
        imageUrl = "https://via.placeholder.com/150",
        iconUrl = null,
        resolutionSource = "Preview Source 1",
        active = true,
        closed = false,
        volume = 1000.0,
        liquidity = 50.0,
        featured = false,
        endDate = OffsetDateTime.now().plusHours(2),
        rawMarkets = listOf(previewMarket)
    )
    PolymarketAppTheme {
        EventListItem(event = previewEvent, onEventClick = {})
    }
}

@Preview(showBackground = true, name = "Multi Market Event Preview (Corrected)")
@Composable
private fun MultiMarketEventListItemPreview() {
    val previewMarket1 = demoOptimizedMarketDto("Will Mark Carney be the next Canadian Prime Minister?", "m1-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.77, 0.23), groupItemTitle = "Mark Carney")
    val previewMarket2 = demoOptimizedMarketDto("Will Pierre Poilievre be the next Canadian Prime Minister?", "m2-slug", active = true, closed = true, listOf("Yes", "No"), listOf(0.24, 0.76), groupItemTitle = "Pierre Poilievre")
    val previewMarket3 = demoOptimizedMarketDto("Will Jagmeet Singh be the next Canadian Prime Minister?", "m3-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.01, 0.99), groupItemTitle = "Jagmeet Singh")
    val previewMarket4 = demoOptimizedMarketDto("Will Someone Else be the next Canadian Prime Minister?", "m4-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.01, 0.99), groupItemTitle = "Someone Else")
    val previewMarket5 = demoOptimizedMarketDto("Will Yet Another Candidate be the next Canadian Prime Minister?", "m5-slug", active = true, closed = false, listOf("Yes", "No"), listOf(0.00, 1.00), groupItemTitle = "Yet Another Candidate")

    val previewEvent = demoOptimizedEventDto(
        id = "event-multi",
        title = "Next Prime Minister of Canada after the election?",
        slug = "event-multi-slug",
        imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Flag_of_Canada_%28Pantone%29.svg/160px-Flag_of_Canada_%28Pantone%29.svg.png",
        startDate = null,
        endDate = OffsetDateTime.now().minusDays(5),
        active = true,
        closed = true,
        rawMarkets = listOf(previewMarket1, previewMarket2, previewMarket3, previewMarket4, previewMarket5)
    )
    PolymarketAppTheme {
        EventListItem(event = previewEvent, onEventClick = {})
    }
}

// NEW Preview for categorical market
@Preview(showBackground = true, name = "Categorical Event Preview")
@Composable
private fun CategoricalEventListItemPreview() {
    val previewMarket = demoOptimizedMarketDto(
        slug = "m-cat-slug",
        active = true,
        closed = false,
    )
    val previewEvent = demoOptimizedEventDto(
        id = "event-cat",
        title = "Game Award Winner",
        slug = "event-cat-slug",
        imageUrl = "https://via.placeholder.com/150/0000FF/FFFFFF?Text=Game",
        startDate = null,
        endDate = OffsetDateTime.now().plusDays(30),
        active = true,
        closed = false,
        rawMarkets = listOf(previewMarket)
    )
    PolymarketAppTheme {
        EventListItem(event = previewEvent, onEventClick = {})
    }
}