package com.streamatico.polymarketviewer.ui.event_list

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.TagDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventListViewModel = hiltViewModel(),
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val tags by viewModel.tagsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val canLoadMore by viewModel.canLoadMore.collectAsState()
    val selectedTagSlug by viewModel.selectedTagSlug.collectAsState()
    val selectedSortOrder by viewModel.selectedSortOrder.collectAsState()
    val areTagsLoading by viewModel.areTagsLoading.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    EventListScreenContent(
        uiState = uiState,
        tags = tags,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        areTagsLoading = areTagsLoading,
        canLoadMore = canLoadMore,
        selectedTagSlug = selectedTagSlug,
        selectedSortOrder = selectedSortOrder,
        onRefresh = viewModel::refreshEvents,
        onLoadMore = viewModel::loadMoreEvents,
        onTagSelected = viewModel::selectTag,
        onSortOrderSelected = viewModel::selectSortOrder,
        onRetry = viewModel::retryLoad,
        onNavigateToEventDetail = onNavigateToEventDetail,
        showBottomSheet = showBottomSheet,
        onShowSortOptionsClick = { showBottomSheet = true },
        onDismissRequest = { showBottomSheet = false },
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToSearch = onNavigateToSearch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListScreenContent(
    uiState: EventListUiState,
    tags: List<TagDto>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    areTagsLoading: Boolean,
    canLoadMore: Boolean,
    selectedTagSlug: String,
    selectedSortOrder: PolymarketEventsSortOrder,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onTagSelected: (String) -> Unit,
    onSortOrderSelected: (PolymarketEventsSortOrder) -> Unit,
    onRetry: () -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    showBottomSheet: Boolean,
    onShowSortOptionsClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    EventListContent(
        uiState = uiState,
        tags = tags,
        isRefreshing = isRefreshing,
        isLoadingMore = isLoadingMore,
        areTagsLoading = areTagsLoading,
        canLoadMore = canLoadMore,
        selectedTagSlug = selectedTagSlug,
        selectedSortOrder = selectedSortOrder,
        onRefresh = onRefresh,
        onLoadMore = onLoadMore,
        onTagSelected = onTagSelected,
        onRetry = onRetry,
        onNavigateToEventDetail = onNavigateToEventDetail,
        onShowSortOptionsClick = onShowSortOptionsClick,
        onNavigateToAbout = onNavigateToAbout,
        onSearchClick = onNavigateToSearch
    )

    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "Sort by",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )

                eventsSortOptions.forEach { (key, label) ->
                    val isSelected = key == selectedSortOrder
                    val isDefault = key == PolymarketEventsSortOrder.DEFAULT_SORT_ORDER

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSortOrderSelected(key)
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        onDismissRequest()
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getIconForSortOrder(key),
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            //modifier = Modifier.weight(1f),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        if(isDefault) {
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListContent(
    uiState: EventListUiState,
    tags: List<TagDto>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    areTagsLoading: Boolean,
    canLoadMore: Boolean,
    selectedTagSlug: String,
    selectedSortOrder: PolymarketEventsSortOrder,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onTagSelected: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToEventDetail: (String) -> Unit,
    onShowSortOptionsClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    // More info: https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation
    val adaptiveInfo = currentWindowAdaptiveInfo()

    val useGrid = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    var lastTagSlug by remember { mutableStateOf<String?>(selectedTagSlug) }
    var lastSortOrder by remember { mutableStateOf(selectedSortOrder) }

    LaunchedEffect(selectedTagSlug, selectedSortOrder) {
        if (lastTagSlug != selectedTagSlug || lastSortOrder != selectedSortOrder) {
            lastTagSlug = selectedTagSlug
            lastSortOrder = selectedSortOrder
            if (useGrid) {
                gridState.scrollToItem(0)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    MyScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(52.dp)
                    )
                },
                title = { Text(stringResource(R.string.app_name), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(id = R.string.search_tooltip)
                            )
                        }
                        IconButton(onClick = onShowSortOptionsClick) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = stringResource(id = R.string.filter_tooltip)
                            )
                        }

                        AboutAction(
                            onNavigateToAbout = onNavigateToAbout
                        )
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TagsRow(
                tags = tags,
                areTagsLoading = areTagsLoading,
                selectedTagSlug = selectedTagSlug,
                onTagSelected = onTagSelected
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is EventListUiState.Loading -> {
                        // Always show LoadingBox when uiState is Loading,
                        // regardless of tags loading status.
                        LoadingBox()
                    }
                    is EventListUiState.Success -> {
                        if (state.events.isEmpty() && !isLoadingMore) {
                            Text("No events found for selected tag.")
                        } else {
                            PullToRefreshBox(
                                isRefreshing = isRefreshing,
                                onRefresh = onRefresh,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val adaptiveInfo = currentWindowAdaptiveInfo()

                                // Determine number of columns for the grid
                                val gridCells = if(adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)) {
                                    GridCells.Adaptive(minSize = 300.dp) // Adaptive columns for Expanded width
                                } else {
                                    GridCells.Fixed(2) // 2 columns for Medium and Compact width
                                }

                                // Use LazyColumn for Compact, LazyVerticalGrid otherwise
                                if (!useGrid) {
                                    LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                        //contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        items(state.events, key = { it.id }) { event ->
                                            EventListItem(
                                                event = event,
                                                onEventClick = onNavigateToEventDetail
                                            )
                                        }
                                        if (isLoadingMore) {
                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                    horizontalArrangement = Arrangement.Center
                                                ) { CircularProgressIndicator() }
                                            }
                                        }
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = gridCells,
                                        state = gridState,
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(state.events, key = { it.id }) { event ->
                                            EventListItem(
                                                event = event,
                                                onEventClick = onNavigateToEventDetail
                                            )
                                        }
                                        if (isLoadingMore) {
                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                    horizontalArrangement = Arrangement.Center
                                                ) { CircularProgressIndicator() }
                                            }
                                        }
                                    }
                                }

                                // Logic for loading more items
                                LaunchedEffect(listState, gridState, canLoadMore, isLoadingMore, isRefreshing, state.events, useGrid) {
                                    snapshotFlow { if (useGrid) gridState.layoutInfo else listState.layoutInfo }
                                        .map { layoutInfo ->
                                            // Explicitly get visibleItemsInfo based on type
                                            val visibleItemsInfo: List<*> = when(layoutInfo) {
                                                is LazyListLayoutInfo -> layoutInfo.visibleItemsInfo
                                                is LazyGridLayoutInfo -> layoutInfo.visibleItemsInfo
                                                else -> emptyList<Unit>()
                                            }
                                            if (visibleItemsInfo.isEmpty()) {
                                                false
                                            } else {
                                                // Get index from the specific item info type
                                                val lastVisibleItemIndex = when(val lastItem = visibleItemsInfo.lastOrNull()) {
                                                    is LazyListItemInfo -> lastItem.index
                                                    is LazyGridItemInfo -> lastItem.index
                                                    else -> -1
                                                }
                                                val totalDataItems = state.events.size
                                                // Adjust threshold based on grid layout if needed, currently uses 5
                                                totalDataItems > 0 && lastVisibleItemIndex != -1 && lastVisibleItemIndex >= totalDataItems - (if (useGrid) 10 else 5)
                                            }
                                        }
                                        .distinctUntilChanged()
                                        .filter { shouldLoadMore -> shouldLoadMore }
                                        .collect {
                                            if (canLoadMore && !isLoadingMore && !isRefreshing) {
                                                onLoadMore()
                                            }
                                        }
                                }
                            }
                        }
                    }
                    is EventListUiState.Error -> {
                        ErrorBox(
                            message = state.message,
                            onRetry = onRetry,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutAction(
    onNavigateToAbout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { showMenu = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(id = R.string.more_options_tooltip)
        )
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            text = { Text(stringResource(id = R.string.about_app_title)) },
            onClick = {
                showMenu = false
                onNavigateToAbout()
            }
        )
    }
}

private val eventsSortOptions: Map<PolymarketEventsSortOrder, String> = mapOf(
    PolymarketEventsSortOrder.VOLUME_24H to "24hr Volume",
    PolymarketEventsSortOrder.VOLUME_TOTAL to "Total Volume",
    PolymarketEventsSortOrder.LIQUIDITY to "Liquidity",
    PolymarketEventsSortOrder.NEWEST to "Newest", // Descending
    PolymarketEventsSortOrder.ENDING_SOON to "Ending Soon", // Ascending
    PolymarketEventsSortOrder.COMPETITIVE to "Competitive"
)

private fun getIconForSortOrder(orderKey: PolymarketEventsSortOrder): ImageVector {
    return when (orderKey) {
        PolymarketEventsSortOrder.VOLUME_24H -> Icons.AutoMirrored.Filled.TrendingUp
        PolymarketEventsSortOrder.VOLUME_TOTAL -> Icons.Filled.LocalFireDepartment
        PolymarketEventsSortOrder.LIQUIDITY -> Icons.Filled.WaterDrop
        PolymarketEventsSortOrder.NEWEST -> Icons.Filled.NewReleases
        PolymarketEventsSortOrder.ENDING_SOON -> Icons.Filled.HourglassBottom
        PolymarketEventsSortOrder.COMPETITIVE -> Icons.Filled.StarOutline
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsRow(
    tags: List<TagDto>,
    areTagsLoading: Boolean,
    selectedTagSlug: String,
    onTagSelected: (String) -> Unit
) {
    val allTag = TagDto(id = POLYMARKET_EVENTS_SLUG_ALL, label = POLYMARKET_EVENTS_SLUG_ALL_TITLE, slug = POLYMARKET_EVENTS_SLUG_ALL, forceShow = true)
    val uniqueTags = tags.filter { !it.slug.equals(POLYMARKET_EVENTS_SLUG_ALL, ignoreCase = true) }
    val displayTags = listOf(allTag) + uniqueTags

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(FilterChipDefaults.Height + 8.dp * 2), // Approximate height for consistency
        contentPadding = PaddingValues(horizontal = 12.dp/*, vertical = 8.dp*/),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (areTagsLoading) {
            item { // Use item within LazyRow to place the indicator
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth() // Take full width available in LazyRow
                        .height(FilterChipDefaults.Height + 8.dp * 2), // Match height
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        } else {
            // Existing items call when not loading
            items(displayTags, key = { it.slug }) { tag ->
                val isSelected = if (tag.slug == POLYMARKET_EVENTS_SLUG_ALL) selectedTagSlug == POLYMARKET_EVENTS_SLUG_ALL else selectedTagSlug == tag.slug
                FilterChip(
                    selected = isSelected,
                    onClick = { onTagSelected(if (tag.slug == "all") POLYMARKET_EVENTS_SLUG_ALL else tag.slug) },
                    label = { Text(tag.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
private fun EventListScreenPreview_Success() {
    val sampleEvents = listOf(
        createSampleEventDto(id = "1", title = "Will event 1 happen?", slug = "event-1", volume = 100.0, liquidity = 50.0),
        createSampleEventDto(id = "2", title = "What about event 2?", slug = "event-2", volume = 200.0, liquidity = 100.0, featured = true, featuredOrder = 1),
        createSampleEventDto(id = "3", title = "A third event?", slug = "event-3", volume = 300.0, liquidity = 150.0)
    )
    val sampleTags = listOf(
        TagDto(id = "tag1", label = "Politics", slug = "politics", forceShow = false),
        TagDto(id = "tag2", label = "Crypto", slug = "crypto", forceShow = false),
        TagDto(id = "tag3", label = "Sports", slug = "sports", forceShow = false)
    )

    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Success(sampleEvents),
            tags = sampleTags,
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = true,
            selectedTagSlug = "crypto",
            selectedSortOrder = PolymarketEventsSortOrder.NEWEST,
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onSortOrderSelected = {},
            onRetry = {},
            onNavigateToEventDetail = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, name = "Loading State")
@Composable
private fun EventListScreenPreview_Loading() {
    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Loading,
            tags = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = false,
            selectedTagSlug = POLYMARKET_EVENTS_SLUG_ALL,
            selectedSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER,
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onSortOrderSelected = {},
            onRetry = {},
            onNavigateToEventDetail = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, name = "Error State")
@Composable
private fun EventListScreenPreview_Error() {
    MaterialTheme {
        EventListScreenContent(
            uiState = EventListUiState.Error("Failed to load events. Please check connection."),
            tags = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            areTagsLoading = false,
            canLoadMore = false,
            selectedTagSlug = POLYMARKET_EVENTS_SLUG_ALL,
            selectedSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER,
            onRefresh = {},
            onLoadMore = {},
            onTagSelected = {},
            onSortOrderSelected = {},
            onRetry = {},
            onNavigateToEventDetail = {},
            showBottomSheet = false,
            onShowSortOptionsClick = {},
            onDismissRequest = {},
            onNavigateToAbout = {},
            onNavigateToSearch = {},
        )
    }
}

private fun createSampleEventDto(id: String, title: String, slug: String, volume: Double? = null, liquidity: Double? = null, featured: Boolean = false, featuredOrder: Int? = null): EventDto {
    return EventDto(
        id = id,
        title = title,
        slug = slug,
        description = "Sample description for $title",
        category = "Sample Category",
        imageUrl = null,
        iconUrl = null,
        active = true,
        closed = false,
        volume = volume,
        liquidity = liquidity,
        startDate = null,
        endDate = null,
        resolutionSource = "Sample Source",
        rawMarkets = emptyList(),
        featured = featured,
        featuredOrder = featuredOrder,
        tags = listOf(TagDto("sample", "Sample Tag", "sample-tag", false))
    )
}