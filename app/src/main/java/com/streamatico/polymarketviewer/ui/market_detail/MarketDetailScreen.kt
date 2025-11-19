package com.streamatico.polymarketviewer.ui.market_detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.data.model.MarketDto
import com.streamatico.polymarketviewer.data.model.getTitleOrDefault
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import java.time.OffsetDateTime

// Market details screen
@Composable
fun MarketDetailScreen(
    viewModel: MarketDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    MarketDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.retryLoad() }
    )
}

// Market details presentation (without ViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketDetailContent(
    uiState: MarketDetailUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    MyScaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (uiState) {
                        is MarketDetailUiState.Success -> uiState.market.getTitleOrDefault("Market Details")
                        is MarketDetailUiState.Loading -> "Loading Details..."
                        is MarketDetailUiState.Error -> "Market Details"
                    }
                    Text(titleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is MarketDetailUiState.Loading -> {
                    LoadingBox()
                }
                is MarketDetailUiState.Success -> {
                    MarketDetailsContent(market = uiState.market, modifier = Modifier.fillMaxSize())
                }
                is MarketDetailUiState.Error -> {
                    ErrorBox(
                        message = uiState.message,
                        onRetry = onRetry,
                    )
                }
            }
        }
    }
}

// Main content on successful load
@Composable
fun MarketDetailsContent(market: MarketDto, modifier: Modifier = Modifier) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    val outcomes = market.outcomes
    val prices = market.outcomePrices
    val outcomePricePairs = remember(outcomes, prices) {
        outcomes.zip(prices) { outcome, price -> outcome to price }
            //.sortedByDescending { it.second }
    }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        // --- Display Market Image --- //
        if(!market.imageUrl.isNullOrBlank()) {
            item {
                AsyncImage(
                    model = market.imageUrl,
                    contentDescription = "Market image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.FillWidth
                )
                Spacer(modifier = Modifier.height(16.dp)) // Space between image and title
            }
        }

        // --- Display Title --- //
        item {
            Text(text = market.question, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Market description (expandable) --- //
        item {
            market.description?.takeIf { it.isNotBlank() }?.let {
                Column {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.animateContentSize()
                    )
                    if (it.lines().size > 5) {
                        val showMoreText = if (isDescriptionExpanded) "Show less" else "Show more"
                        Text(
                            text = showMoreText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // --- Additional market information (in card) --- //
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    market.volume?.let {
                        InfoRow(label = "Volume", value = UiFormatter.formatLargeValueUsd(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.liquidity?.let {
                        InfoRow(label = "Liquidity", value = UiFormatter.formatLargeValueUsd(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.startDate?.let {
                        InfoRow(label = "Starts", value = UiFormatter.formatDateTimeLong(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.endDate?.let {
                        InfoRow(label = "Ends", value = UiFormatter.formatDateTimeLong(it))
                        Spacer(Modifier.height(8.dp))
                    }
                    market.umaResolutionStatus?.let {
                        InfoRow(label = "Resolution Status", value = it)
                    }
                    market.resolutionSource?.takeIf { it.isNotBlank() }?.let {
                        // Remove Spacer after the last element
                        InfoRow(label = "Resolution Source", value = it)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacing after card
            // HorizontalDivider() // Remove divider here
        }

        // --- Outcomes list --- //
        if (outcomePricePairs.isNotEmpty()) {
            item { // Header for outcomes
                Text("Outcomes:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                HorizontalDivider() // Keep divider above list
            }
            items(outcomePricePairs) { (outcome, price) ->
                MarketOutcomeRow(
                    outcomeText = outcome,
                    price = price,
                    //marketVolume = market.volume
                )
                HorizontalDivider() // Divider between outcomes
            }
        }
    }
}

// -- Helper Composables and functions -- //

// Row for displaying information (Label: Value)
@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

// Row for displaying market outcome
@Composable
private fun MarketOutcomeRow(
    outcomeText: String,
    price: Double?,
    //marketVolume: Double?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = outcomeText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = UiFormatter.formatPriceAsPercentage(price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        price?.let {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { it.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- Previews --- //

@Preview(showBackground = true, name = "Success - Short Description")
@Composable
private fun MarketDetailViewPreviewSuccessShortDesc() {
    val shortDescMarket = PreviewMocks.sampleMarket1.copy(
        id = "2",
        question = "Market with short description?",
        slug = "market-with-short-description",
        description = "Short and sweet.",
        startDate = OffsetDateTime.now(),
        endDate = null,
        resolutionSource = "Community",
        volume = 5000.0,
        liquidity = 1000.0,
        outcomePricesJson = """["0.60", "0.40"]"""
    )
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Success(shortDescMarket),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Success - No Volume/Liquidity")
@Composable
private fun MarketDetailViewPreviewSuccessNoMetrics() {
    val noMetricsMarket = PreviewMocks.sampleMarket1.copy(
        question = "Market without volume/liquidity?",
        description = "Description here.",
        volume = null, // No volume
        liquidity = null, // No liquidity
        outcomesJson = """["Outcome A", "Outcome B"]""", // For scalar, might be different
        outcomePricesJson = """["0.5", "0.5"]""" // Placeholder prices
    )
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Success(noMetricsMarket),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun MarketDetailViewPreviewLoading() {
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Loading,
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State")
@Composable
private fun MarketDetailViewPreviewSuccess() { // Removed PreviewParameter
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Success(PreviewMocks.sampleMarket1), // Use the sample market directly
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun MarketDetailViewPreviewError() {
    MaterialTheme { // Wrap preview in MaterialTheme
        MarketDetailContent(
            uiState = MarketDetailUiState.Error("Failed to load market details."),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}
