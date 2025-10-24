package com.streamatico.polymarketviewer.ui.event_detail

import android.content.Intent
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Legend
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.streamatico.polymarketviewer.data.model.CommentCreatorProfileDto
import com.streamatico.polymarketviewer.data.model.CommentDto
import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.EventType
import com.streamatico.polymarketviewer.data.model.MarketResolutionStatus
import com.streamatico.polymarketviewer.data.model.PositionDto
import com.streamatico.polymarketviewer.data.model.TagDto
import com.streamatico.polymarketviewer.data.model.demoMarketDto
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.sortedByViewPriority
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Define constant for market limit
private const val MARKET_DISPLAY_LIMIT = 5

@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToMarketDetail: (String) -> Unit,
    onNavigateToUserProfile: (profileAddress: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    // Collect hierarchical comments state
    val hierarchicalComments by viewModel.hierarchicalCommentsState.collectAsState()
    // Collect other states as before
    val commentsLoading by viewModel.commentsLoading.collectAsState()
    val commentsError by viewModel.commentsError.collectAsState()
    val holdersOnly by viewModel.holdersOnly.collectAsState()
    val canLoadMoreComments by viewModel.canLoadMoreComments.collectAsState()
    // Collect the token map
    val eventOutcomeTokensMap by viewModel.eventOutcomeTokensMap.collectAsState()
    val eventTokenToGroupTitleMap by viewModel.eventTokenToGroupTitleMap.collectAsState()

    EventDetailsContent(
        uiState = uiState,
        onMarketClick = onNavigateToMarketDetail,
        chartModelProducer = viewModel.chartModelProducer,
        selectedRange = selectedTimeRange,
        onRangeSelected = viewModel::selectTimeRange,
        // Pass hierarchical comments
        displayableComments = hierarchicalComments,
        // Pass other states/callbacks as before
        commentsLoading = commentsLoading,
        commentsError = commentsError,
        onNavigateToUserProfile = onNavigateToUserProfile,
        holdersOnly = holdersOnly,
        canLoadMoreComments = canLoadMoreComments,
        onToggleHoldersOnly = viewModel::toggleHoldersOnly,
        onLoadMoreComments = viewModel::loadMoreComments,
        onRefreshComments = viewModel::refreshComments,
        onNavigateBack = onNavigateBack,
        onRetry = { viewModel.retryLoad() },
        // Pass the token map
        eventOutcomeTokensMap = eventOutcomeTokensMap,
        eventTokenToGroupTitleMap = eventTokenToGroupTitleMap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventDetailsContent(
    uiState: EventDetailUiState,
    onMarketClick: (String) -> Unit,
    chartModelProducer: CartesianChartModelProducer,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    // Receive hierarchical comments
    displayableComments: List<HierarchicalComment>,
    // Receive other states/callbacks
    commentsLoading: Boolean,
    commentsError: String?,
    onNavigateToUserProfile: (profileAddress: String) -> Unit,
    holdersOnly: Boolean,
    canLoadMoreComments: Boolean,
    onToggleHoldersOnly: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onRefreshComments: () -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    // Receive the token map
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>
) {
    val context = LocalContext.current
    // Get current configuration and primary locale
    val configuration = LocalConfiguration.current
    val primaryLocale = configuration.locales[0]
    // Determine if the device language is English
    val isEnglishLocale = primaryLocale.language == "en"

    // Hoist LazyListState here
    val listState = rememberLazyListState()
    // Get event title (if available)
    val eventTitle = (uiState as? EventDetailUiState.Success)?.event?.title
    // Determine if title should be shown in AppBar
    // Show title in app bar if the second item (index 1, the title) is no longer the first visible item
    val showTitleInAppBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Show event title if scrolled past the main title, otherwise show "Event Details"
                    val titleText = if (showTitleInAppBar && eventTitle != null) eventTitle else "Event Details"
                    Text(titleText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    /* // Static title
                    Text("Event Details")
                    */
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Translate Action - Use composable from TranslateAction.kt
                    TranslateAction(
                        isVisible = !isEnglishLocale && uiState is EventDetailUiState.Success,
                        event = (uiState as? EventDetailUiState.Success)?.event,
                    )

                    // Open in Browser Action
                    if (uiState is EventDetailUiState.Success) {
                        uiState.event.slug.let { slug ->
                            val url = "https://polymarket.com/event/$slug"
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("EventDetailScreen", "Failed to open URL: $url", e)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.OpenInNew,
                                    contentDescription = "Open in browser"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is EventDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingBox()
                }
            }
            is EventDetailUiState.Success -> {
                EventDetailsContentSuccess(
                    listState = listState, // Pass listState down
                    event = state.event,
                    modifier = Modifier.padding(paddingValues),
                    onMarketClick = onMarketClick,
                    chartModelProducer = chartModelProducer,
                    selectedRange = selectedRange,
                    onRangeSelected = onRangeSelected,
                    // Pass hierarchical comments
                    displayableComments = displayableComments,
                    // Pass other states/callbacks as before
                    commentsLoading = commentsLoading,
                    commentsError = commentsError,
                    onNavigateToUserProfile = onNavigateToUserProfile,
                    holdersOnly = holdersOnly,
                    canLoadMoreComments = canLoadMoreComments,
                    onToggleHoldersOnly = onToggleHoldersOnly,
                    onLoadMoreComments = onLoadMoreComments,
                    onRefreshComments = onRefreshComments,
                    // Pass the token map
                    eventOutcomeTokensMap = eventOutcomeTokensMap,
                    eventTokenToGroupTitleMap = eventTokenToGroupTitleMap
                )
            }
            is EventDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorBox(
                        message = state.message,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailsContentSuccess(
    listState: LazyListState, // Receive listState
    event: EventDto,
    modifier: Modifier = Modifier,
    onMarketClick: (String) -> Unit,
    chartModelProducer: CartesianChartModelProducer,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    // Receive hierarchical comments
    displayableComments: List<HierarchicalComment>,
    // Receive other states/callbacks
    commentsLoading: Boolean,
    commentsError: String?,
    onNavigateToUserProfile: (profileAddress: String) -> Unit,
    holdersOnly: Boolean,
    canLoadMoreComments: Boolean,
    onToggleHoldersOnly: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onRefreshComments: () -> Unit,
    // Receive the token map
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    // Add state for market list expansion
    var isMarketListExpanded by rememberSaveable { mutableStateOf(false) }

    val marketPrices = remember(event.markets) {
        event.markets.associate { market ->
            market.id to  market.yesPrice
        }
    }
    val sortedMarkets = remember(event.markets) {
         event.markets.sortedByViewPriority(event.sortByEnum)
    }
    val totalEventVolume = event.volume

    // Determine which markets to display
    val visibleMarkets = if (sortedMarkets.size > MARKET_DISPLAY_LIMIT && !isMarketListExpanded) {
        sortedMarkets.take(MARKET_DISPLAY_LIMIT)
    } else {
        sortedMarkets
    }
    val hiddenMarketCount = sortedMarkets.size - MARKET_DISPLAY_LIMIT

    LazyColumn(
        state = listState, // Use passed state
        modifier = modifier.fillMaxSize(),
        //contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        // Event title
        item {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = event.title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Event image
        if (event.imageUrl != null) {
            item {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Event description
        item {
            event.description?.takeIf { it.isNotBlank() }?.let {
                Column {
                    SelectionContainer {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 5,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.animateContentSize()
                        )
                    }
                    val showMoreText = if (isDescriptionExpanded) "Show less" else "Show more"
                    if (it.lines().size > 5) {
                        Text(
                            text = showMoreText,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 4.dp)
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        )
                    }
                }
                //Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Event properties
        item {
            Column(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                event.volume?.let {
                    InfoRow(label = "Total Volume", value = UiFormatter.formatLargeValueUsd(it))
                    Spacer(Modifier.height(8.dp))
                }
                event.category?.let {
                    InfoRow(label = "Category", value = it)
                    Spacer(Modifier.height(8.dp))
                }
                event.startDate?.let {
                    InfoRow(label = "Starts", value = UiFormatter.formatDateTimeLong(it))
                    Spacer(Modifier.height(8.dp))
                }
                event.endDate?.let {
                    InfoRow(label = "Ends", value = UiFormatter.formatDateTimeLong(it))
                    Spacer(Modifier.height(8.dp))
                }
                event.resolutionSource?.takeIf { it.isNotBlank() }?.let {
                    InfoRow(label = "Resolution Source", value = it)
                }
            }
        }

        // Chart
        item {
            //Spacer(modifier = Modifier.height(16.dp))
            PriceHistoryChart(
                chartModelProducer = chartModelProducer,
                event = event,
                selectedRange = selectedRange,
                onRangeSelected = onRangeSelected,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Event markets (outcomes)
        if (visibleMarkets.isNotEmpty()) {
            item {
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

            items(visibleMarkets, key = { it.id }) { market ->
                val price = marketPrices[market.id]

                val outcomeText = if (sortedMarkets.size == 1) market.getYesTitle()
                else market.getTitleOrDefault("Unknown")

                EventMarketRow(
                    outcomeText = outcomeText,
                    iconUrl = market.iconUrl,
                    resolutionStatus = market.getResolutionStatus(),
                    price = price,
                    volume = market.volume ?: totalEventVolume,
                    showIcon = event.showMarketImages && event.eventType != EventType.BinaryEvent,
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
                            .clickable { isMarketListExpanded = !isMarketListExpanded }
                    )
                }
            }
        }

        CommentsSection(
            displayableComments = displayableComments,
            // Receive other states/callbacks
            commentsLoading = commentsLoading,
            commentsError = commentsError,
            onNavigateToUserProfile = onNavigateToUserProfile,
            onRefreshComments = onRefreshComments,
            // Receive the token map
            eventOutcomeTokensMap = eventOutcomeTokensMap,
            eventTokenToGroupTitleMap = eventTokenToGroupTitleMap,
            eventType = event.eventType,

            holdersOnly = holdersOnly,
            onToggleHoldersOnly = onToggleHoldersOnly,
        )
    }

    LaunchedEffect(listState, commentsLoading, canLoadMoreComments, displayableComments) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                if (layoutInfo.totalItemsCount == 0 || visibleItemsInfo.isEmpty()) {
                    false
                } else {
                    val lastVisibleItemIndex = visibleItemsInfo.last().index
                    lastVisibleItemIndex >= layoutInfo.totalItemsCount - 3
                }
            }
            .distinctUntilChanged()
            .filter { shouldLoadMore -> shouldLoadMore }
            .collect {
                if (canLoadMoreComments && !commentsLoading) {
                    Log.d("EventDetailsContent", "Requesting loadMoreComments")
                    onLoadMoreComments()
                }
            }
    }
}

//@Composable
private fun LazyListScope.CommentsSection(
    displayableComments: List<HierarchicalComment>,
    // Receive other states/callbacks
    commentsLoading: Boolean,
    commentsError: String?,
    onNavigateToUserProfile: (profileAddress: String) -> Unit,
    onRefreshComments: () -> Unit,
    // Receive the token map
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>,
    eventType: EventType,

    holdersOnly: Boolean,
    onToggleHoldersOnly: () -> Unit,
) {
    // Comments header
    item {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Comments", style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onToggleHoldersOnly() }
            ) {
                Checkbox(
                    checked = holdersOnly,
                    onCheckedChange = { onToggleHoldersOnly() }
                )
                Text("Holders only", style = MaterialTheme.typography.bodyMedium)
            }
        }
        HorizontalDivider()
    }

    // Comments list
    if (displayableComments.isNotEmpty()) {
        items(displayableComments, key = { it.comment.id }) { hierarchicalComment ->
            CommentItem(
                hierarchicalComment = hierarchicalComment,
                eventOutcomeTokensMap = eventOutcomeTokensMap,
                eventTokenToGroupTitleMap = eventTokenToGroupTitleMap,
                onUserProfileClick = { profile ->
                    onNavigateToUserProfile(profile.proxyWallet)
                },
                isBinaryEvent = eventType == EventType.BinaryEvent,
            )
            HorizontalDivider()
        }
    }

    // Comments loading and error states
    item {
        if (commentsLoading && displayableComments.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (commentsError != null && displayableComments.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error loading comments: $commentsError", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRefreshComments) { Text("Retry") }
            }
        } else if (!commentsLoading && displayableComments.isEmpty()) {
            Text(
                "No comments found.",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EventMarketRow(
    //market: MarketDto,
    outcomeText: String,
    iconUrl: String?,
    resolutionStatus: MarketResolutionStatus?,
    price: Double?,
    volume: Double?,
    showIcon: Boolean,
    onClick: () -> Unit
) {
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

                    if(resolutionStatus != null) {
                        Spacer(modifier = Modifier.width(8.dp))

                        val resolutionText = when (resolutionStatus) {
                            MarketResolutionStatus.DISPUTED -> "(Disputed)"
                            MarketResolutionStatus.RESOLVED -> "(Resolved)"
                        }

                        Text(
                            text = resolutionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                 volume?.let {
                     Text(
                         text = UiFormatter.formatLargeValueUsd(it, suffix = " Vol."),
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.outline,
                         modifier = Modifier.padding(top = 2.dp)
                     )
                 }
            }
            Spacer(modifier = Modifier.width(16.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceHistoryChart(
    chartModelProducer: CartesianChartModelProducer,
    event: EventDto,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
) {
    val timeRanges = TimeRange.entries
        .filter { it != TimeRange.H6 }
        .toTypedArray()

    val lineColors = remember(event.markets.size) {
        (0 until event.markets.size).map { index ->
            Color.hsl(
                hue = (index * (360f / (event.markets.size.takeIf { it > 0 } ?: 1))) % 360f,
                saturation = 0.9f,
                lightness = 0.6f
            )
        }
    }

    val xAxisValueFormatter = remember(selectedRange) {
        CartesianValueFormatter { context: CartesianMeasuringContext, value: Double, _ ->
            val timestampSeconds = value.toLong()

            val instant = Instant.ofEpochSecond(timestampSeconds)
            val zoneId = ZoneId.systemDefault()
            val pattern = when (selectedRange) {
                TimeRange.H1, TimeRange.H6, TimeRange.D1 -> "HH:mm"
                TimeRange.W1, TimeRange.M1 -> "dd MMM"
                TimeRange.ALL -> "MMM uuuu"
            }
            val formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId)
            formatter.format(instant)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center
        ) {
             SingleChoiceSegmentedButtonRow {
                timeRanges
                    .forEachIndexed { index, range ->

                    val rangeName = when (range) {
                        TimeRange.H1 -> "1H"
                        TimeRange.H6 -> "6H"
                        TimeRange.D1 -> "1D"
                        TimeRange.W1 -> "1W"
                        TimeRange.M1 -> "1M"
                        TimeRange.ALL -> "ALL"
                    }

                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = timeRanges.size),
                        onClick = { onRangeSelected(range) },
                        selected = range == selectedRange
                    ) {
                        Text(rangeName)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        val lineLayer = rememberLineCartesianLayer(
            LineCartesianLayer.LineProvider.series(
                lineColors.map { color ->
                    LineCartesianLayer.rememberLine(LineCartesianLayer.LineFill.single(fill(color)))
                }
            ),
            /*
            rangeProvider = CartesianLayerRangeProvider.fixed(
                minY = 0.0,
                maxY = 100.0
            )
             */
            rangeProvider = EventChartRangeProvider
        )

        CartesianChartHost(
            modifier = Modifier.height(280.dp),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            chart = rememberCartesianChart(
                lineLayer,
                marker = rememberChartMarker(),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = CartesianValueFormatter { context: CartesianMeasuringContext, value: Double, _ ->
                        "${value.toInt()}%"
                    }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = xAxisValueFormatter,
                    itemPlacer = remember {
                        HorizontalAxis.ItemPlacer.aligned()
                    }
                ),

                legend = rememberEventChartLegend(
                    showLegend = event.markets.size > 1,
                    lineColors = lineColors
                ),
            ),
            modelProducer = chartModelProducer,
            placeholder = {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
        )
    }
}

@Composable
private fun rememberEventChartLegend(
    showLegend: Boolean,
    lineColors: List<Color>
) : Legend<CartesianMeasuringContext, CartesianDrawingContext>? {
    if(!showLegend) {
        return null
    }

    val legendItemLabelComponent = rememberTextComponent(vicoTheme.textColor)

    return rememberHorizontalLegend(
        items = { extraStore ->
            extraStore[LegendLabelKey]
                .toList()
                .sortedBy { it.order }
                .forEachIndexed { index, orderedLabel ->
                    add(
                        LegendItem(
                            shapeComponent(fill(lineColors[index]), CorneredShape.Pill),
                            legendItemLabelComponent,
                            orderedLabel.label,
                        )
                    )
                }
        },
        padding = insets(top = 16.dp),
    )
}


// --- Previews --- //

private val sampleUserProfile = CommentCreatorProfileDto(
    name = "CryptoChad",
    pseudonym = "CryptoChad",
    profileImage = "https://via.placeholder.com/150/0000FF/808080?Text=User+Avatar",
    proxyWallet = "0x123abc456def7890",
    positions = listOf(
        PositionDto(
            tokenId = "token-yes-1",
            positionSize = "500000000" // 500 USDC
        ),
        PositionDto(
            tokenId = "token-no-2",
            positionSize = "1000000000" // 1000 USDC
        )
    ),
    displayUsernamePublic = true,
    bio = "Crypto enthusiast and trader.",
)

private val sampleComment1 = CommentDto(
    id = "comment-1",
    body = "This looks promising! Going long here.",
    createdAt = OffsetDateTime.now().minusHours(2),
    profile = sampleUserProfile,
    parentCommentID = null,
    parentEntityID = null,
    parentEntityType = null,
    updatedAt = OffsetDateTime.now().minusMinutes(30),
    userAddress = null,
    replyAddress = null,
    reportCount = 10,
    reactionCount = 3,
)

private val sampleReply1 = CommentDto(
    id = "reply-1",
    body = "Disagree, I think it's going down.",
    createdAt = OffsetDateTime.now().minusMinutes(30),
    profile = sampleUserProfile.copy(name = "BearishBob", proxyWallet = "0xabc123def456fed789"),
    parentEntityType = null,
    parentEntityID = null,
    parentCommentID = null,
    reactionCount = 5,
    updatedAt = null,
    userAddress = null,
    replyAddress = null,
    reportCount = 0,
)

private val sampleComment2 = CommentDto(
    id = "comment-2",
    body = "What does everyone think about the latest news?",
    createdAt = OffsetDateTime.now().minusDays(1),
    profile = sampleUserProfile.copy(name = "NewsNancy", proxyWallet = "0xfed789abc123def456", profileImage = "https://via.placeholder.com/150/FF0000/FFFFFF?Text=NN"),
    parentEntityType = null,
    parentEntityID = null,
    parentCommentID = null,
    reactionCount = 12,
    updatedAt = null,
    userAddress = null,
    replyAddress = null,
    reportCount = 2,
)

private val sampleHierarchicalComments = listOf<HierarchicalComment>(
    HierarchicalComment(sampleComment1, listOf(sampleReply1)),
    HierarchicalComment(sampleComment2)
)

private val sampleMarket1 = demoMarketDto(
    id = "market-1",
    question = "Will the price reach $100k?",
    slug = "price-100k",
    description = "Market for price prediction.",
    startDate = OffsetDateTime.now().minusDays(5),
    endDate = OffsetDateTime.now().plusDays(30),
    resolutionSource = "Coinbase",
    volume = 500000.0,
    liquidity = 20000.0,
    outcomesJson = "[\"Yes\", \"No\"]",
    outcomePricesJson = "[\"0.65\", \"0.35\"]",
    active = true,
    closed = false,
)

private val sampleMarket2 = demoMarketDto(
    id = "market-2",
    question = "Will it close above $90k?",
    slug = "price-90k",
    outcomesJson = "[\"Yes\", \"No\"]",
    outcomePricesJson = "[\"0.80\", \"0.20\"]",
    volume = 300000.0,
    liquidity = 15000.0,

    lastTradePrice = 0.79,
    bestBid = 0.78,
    bestAsk = 0.81
)

private val sampleEvent = EventDto(
    id = "event-1",
    title = "Bitcoin Price Prediction End of Year",
    slug = "bitcoin-price-prediction-eoy",
    description = "Predict the price of Bitcoin by the end of the current year. This event covers multiple price targets.",
    category = "Crypto",
    imageUrl = "https://via.placeholder.com/600x300/0000FF/FFFFFF?Text=Event+Image",
    iconUrl = null,
    active = true,
    closed = false,
    volume = 800000.0, // Sum of market volumes
    liquidity = 35000.0, // Sum of market liquidities
    startDate = OffsetDateTime.now().minusDays(10),
    endDate = OffsetDateTime.now().plusDays(60),
    resolutionSource = "Multiple Exchanges Average",
    rawMarkets = listOf(sampleMarket1, sampleMarket2),
    featured = true,
    featuredOrder = 1,
    tags = listOf(TagDto(id = "tag-crypto", label = "Crypto", slug = "crypto", forceShow = false))
)

// Mock ChartModelProducer for previews
private val previewChartModelProducer = CartesianChartModelProducer()

@Composable
private fun EventDetailsContentPreviewTemplate(uiState: EventDetailUiState) {
    MaterialTheme {
        EventDetailsContent(
            uiState = uiState,
            onMarketClick = { },
            chartModelProducer = previewChartModelProducer, // Use the mock producer
            selectedRange = TimeRange.D1,
            onRangeSelected = { },
            displayableComments = sampleHierarchicalComments,
            commentsLoading = false,
            commentsError = null,
            onNavigateToUserProfile = { },
            holdersOnly = false,
            canLoadMoreComments = true,
            onToggleHoldersOnly = { },
            onLoadMoreComments = { },
            onRefreshComments = { },
            onNavigateBack = { },
            onRetry = { },
            eventOutcomeTokensMap = mapOf("token-yes-1" to "Yes", "token-no-2" to "No"),
            eventTokenToGroupTitleMap = mapOf("token-yes-1" to "$100k?", "token-no-2" to "$90k?")
        )
    }
}

@Preview(showBackground = true, name = "Event Detail - Success")
@Composable
private fun EventDetailScreenPreviewSuccess() {
    EventDetailsContentPreviewTemplate(
        uiState = EventDetailUiState.Success(sampleEvent)
    )
}

@Preview(showBackground = true, name = "Event Detail - Loading")
@Composable
private fun EventDetailScreenPreviewLoading() {
    EventDetailsContentPreviewTemplate(
        uiState = EventDetailUiState.Loading
    )
}

@Preview(showBackground = true, name = "Event Detail - Error")
@Composable
private fun EventDetailScreenPreviewError() {
    EventDetailsContentPreviewTemplate(
        uiState = EventDetailUiState.Error("Failed to load event data.")
    )
}
