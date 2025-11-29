package com.streamatico.polymarketviewer.ui.event_detail

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventType
import com.streamatico.polymarketviewer.domain.repository.CommentsSortOrder
import com.streamatico.polymarketviewer.ui.event_detail.components.EventChartSection
import com.streamatico.polymarketviewer.ui.event_detail.components.eventCommentsSection
import com.streamatico.polymarketviewer.ui.event_detail.components.EventHeader
import com.streamatico.polymarketviewer.ui.event_detail.components.eventDetailMarketList
import com.streamatico.polymarketviewer.ui.event_detail.components.TranslateAction
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.shared.components.OpenInBrowserIconButton
import com.streamatico.polymarketviewer.ui.shared.sortedByViewPriority
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

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
    val commentsSortOrder by viewModel.commentsSortOrder.collectAsState()
    val canLoadMoreComments by viewModel.canLoadMoreComments.collectAsState()
    // Collect the token map
    val eventOutcomeTokensMap by viewModel.eventOutcomeTokensMap.collectAsState()
    val eventTokenToGroupTitleMap by viewModel.eventTokenToGroupTitleMap.collectAsState()

    val isChartAvailable by viewModel.isChartAvailable.collectAsState()

    EventDetailsContent(
        uiState = uiState,
        onMarketClick = onNavigateToMarketDetail,
        chartModelProducer = viewModel.chartModelProducer,
        isChartAvailable = isChartAvailable,
        selectedRange = selectedTimeRange,
        onRangeSelected = viewModel::selectTimeRange,
        // Pass hierarchical comments
        displayableComments = hierarchicalComments,
        // Pass other states/callbacks as before
        commentsLoading = commentsLoading,
        commentsError = commentsError,
        onNavigateToUserProfile = onNavigateToUserProfile,
        holdersOnly = holdersOnly,
        commentsSortOrder = commentsSortOrder,
        canLoadMoreComments = canLoadMoreComments,
        onToggleHoldersOnly = viewModel::toggleHoldersOnly,
        onCommentsSortOrderChange = viewModel::selectCommentsSortOrder,
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
    isChartAvailable: Boolean,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    // Receive hierarchical comments
    displayableComments: List<HierarchicalComment>,
    // Receive other states/callbacks
    commentsLoading: Boolean,
    commentsError: String?,
    onNavigateToUserProfile: (profileAddress: String) -> Unit,
    holdersOnly: Boolean,
    commentsSortOrder: CommentsSortOrder,
    canLoadMoreComments: Boolean,
    onToggleHoldersOnly: () -> Unit,
    onCommentsSortOrderChange: (CommentsSortOrder) -> Unit,
    onLoadMoreComments: () -> Unit,
    onRefreshComments: () -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    // Receive the token map
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>
) {
    // Determine if the device language is English
    val isEnglishLocale = Locale.current.language == "en"

    // Hoist LazyListState here
    val listState = rememberLazyListState()
    // Get event title (if available)
    val eventTitle = (uiState as? EventDetailUiState.Success)?.event?.title
    // Determine if title should be shown in AppBar
    // Show title in app bar if the second item (index 1, the title) is no longer the first visible item
    val showTitleInAppBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    MyScaffold(
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
                            OpenInBrowserIconButton(
                                "https://polymarket.com/event/$slug"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
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
                    event = uiState.event,
                    modifier = Modifier.padding(paddingValues),
                    onMarketClick = onMarketClick,
                    chartModelProducer = chartModelProducer,
                    isChartAvailable = isChartAvailable,
                    selectedRange = selectedRange,
                    onRangeSelected = onRangeSelected,
                    // Pass hierarchical comments
                    displayableComments = displayableComments,
                    // Pass other states/callbacks as before
                    commentsLoading = commentsLoading,
                    commentsError = commentsError,
                    onNavigateToUserProfile = onNavigateToUserProfile,
                    holdersOnly = holdersOnly,
                    commentsSortOrder = commentsSortOrder,
                    canLoadMoreComments = canLoadMoreComments,
                    onToggleHoldersOnly = onToggleHoldersOnly,
                    onCommentsSortOrderChange = onCommentsSortOrderChange,
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
                        message = uiState.message,
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
    isChartAvailable: Boolean,
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    // Receive hierarchical comments
    displayableComments: List<HierarchicalComment>,
    // Receive other states/callbacks
    commentsLoading: Boolean,
    commentsError: String?,
    onNavigateToUserProfile: (profileAddress: String) -> Unit,
    holdersOnly: Boolean,
    commentsSortOrder: CommentsSortOrder,
    canLoadMoreComments: Boolean,
    onToggleHoldersOnly: () -> Unit,
    onCommentsSortOrderChange: (CommentsSortOrder) -> Unit,
    onLoadMoreComments: () -> Unit,
    onRefreshComments: () -> Unit,
    // Receive the token map
    eventOutcomeTokensMap: Map<String, String>,
    eventTokenToGroupTitleMap: Map<String, String>
) {
    // Add state for market list expansion
    var isMarketListExpanded by rememberSaveable { mutableStateOf(false) }

    val sortedMarkets = remember(event.markets) {
         event.markets.sortedByViewPriority(event.sortByEnum)
    }

    LazyColumn(
        state = listState, // Use passed state
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        // Header (Title, Image, Description, Info)
        item {
            EventHeader(event = event)
        }

        if(isChartAvailable) {
            // Chart
            item {
                EventChartSection(
                    chartModelProducer = chartModelProducer,
                    event = event,
                    selectedRange = selectedRange,
                    onRangeSelected = onRangeSelected,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Event markets (outcomes)
        eventDetailMarketList(
            sortedMarkets = sortedMarkets,
            showMarketImages = event.showMarketImages && event.eventType != EventType.BinaryEvent,
            isMarketListExpanded = isMarketListExpanded,
            onMarketExpandToggle = {
                isMarketListExpanded = !isMarketListExpanded
            },
            onMarketClick = onMarketClick
        )

        eventCommentsSection(
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
            commentsSortOrder = commentsSortOrder,
            onToggleHoldersOnly = onToggleHoldersOnly,
            onCommentsSortOrderChange = onCommentsSortOrderChange,
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


// --- Previews --- //

@Composable
private fun EventDetailsContentPreviewTemplate(uiState: EventDetailUiState) {
    MaterialTheme {
        EventDetailsContent(
            uiState = uiState,
            onMarketClick = { },
            chartModelProducer = PreviewMocks.previewChartModelProducer, // Use the mock producer
            isChartAvailable = true,
            selectedRange = TimeRange.D1,
            onRangeSelected = { },
            displayableComments = PreviewMocks.sampleHierarchicalComments,
            commentsLoading = false,
            commentsError = null,
            onNavigateToUserProfile = { },
            holdersOnly = false,
            commentsSortOrder = CommentsSortOrder.NEWEST,
            canLoadMoreComments = true,
            onToggleHoldersOnly = { },
            onCommentsSortOrderChange = { },
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
        uiState = EventDetailUiState.Success(PreviewMocks.sampleEvent1)
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
