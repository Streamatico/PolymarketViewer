package com.streamatico.polymarketviewer.ui.event_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.streamatico.polymarketviewer.data.model.clob_api.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.getTitleOrDefault
import com.streamatico.polymarketviewer.data.model.gamma_api.yesPrice
import com.streamatico.polymarketviewer.domain.repository.CommentsParentEntityId
import com.streamatico.polymarketviewer.domain.repository.CommentsParentEntityType
import com.streamatico.polymarketviewer.domain.repository.CommentsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.navigation.NavKeys

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class EventDetailViewModel(
    private val polymarketRepository: PolymarketRepository,
    navKey: NavKeys.EventDetail
) : ViewModel() {

    // Get event Slug from navigation arguments
    private val eventSlug: String = navKey.eventSlug

    // --- Event Details State --- //
    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    // Map for determining badge color (Token ID -> Outcome Label like "Yes"/"No")
    private val _eventOutcomeTokensMap = MutableStateFlow<Map<String, String>>(emptyMap())
    // Expose the outcome map state flow publicly
    val eventOutcomeTokensMap: StateFlow<Map<String, String>> = _eventOutcomeTokensMap.asStateFlow()

    private val _isChartAvailable = MutableStateFlow(true)
    val isChartAvailable: StateFlow<Boolean> = _isChartAvailable.asStateFlow()

    // Map for displaying badge text (Token ID -> Market Title like "Pierre Poilievre")
    private val _eventTokenToGroupTitleMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val eventTokenToGroupTitleMap: StateFlow<Map<String, String>> = _eventTokenToGroupTitleMap.asStateFlow()

    // --- Chart State --- //
    val chartModelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    private val _selectedTimeRange = MutableStateFlow(TimeRange.M1)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    // --- Comments State (Keep flat list for loading/pagination) --- //
    private val _commentsState = MutableStateFlow<List<CommentDto>>(emptyList())

    // --- Derived Hierarchical Comments State --- //
    val hierarchicalCommentsState: StateFlow<List<HierarchicalComment>> = _commentsState
        .map { flatList -> processComments(flatList) } // Process flat list
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Cache when subscribers are present
            initialValue = emptyList() // Initial empty hierarchy
        )

    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()

    private val _commentsError = MutableStateFlow<String?>(null)
    val commentsError: StateFlow<String?> = _commentsError.asStateFlow()

    // Default holdersOnly to true
    private val _holdersOnly = MutableStateFlow(true) // Changed initial value to true
    val holdersOnly: StateFlow<Boolean> = _holdersOnly.asStateFlow()

    // Default sort order to NEWEST
    private val _commentsSortOrder = MutableStateFlow(CommentsSortOrder.NEWEST)
    val commentsSortOrder: StateFlow<CommentsSortOrder> = _commentsSortOrder.asStateFlow()

    private val _canLoadMoreComments = MutableStateFlow(true)
    val canLoadMoreComments: StateFlow<Boolean> = _canLoadMoreComments.asStateFlow()
    private var commentsOffset = 0

    init {
        loadEventDetailsAndInitialData()
    }

    private fun loadEventDetailsAndInitialData() {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            _eventOutcomeTokensMap.value = emptyMap() // Reset maps
            _eventTokenToGroupTitleMap.value = emptyMap()
            val eventResult = polymarketRepository.getEventDetailsBySlug(eventSlug)

            eventResult.onSuccess {
                _uiState.value = EventDetailUiState.Success(it)
                // Process event markets to build both token maps
                val (outcomeMap, titleMap) = buildTokenMaps(it)
                _eventOutcomeTokensMap.value = outcomeMap
                _eventTokenToGroupTitleMap.value = titleMap
                // Load initial chart data and comments
                loadChartData(_selectedTimeRange.value)
                refreshComments()
            }.onFailure {
                _uiState.value = EventDetailUiState.Error(it.message ?: "Failed to load event details")
            }
        }
    }

    // Renamed helper to build BOTH Token maps using Kotlinx Serialization
    private fun buildTokenMaps(event: EventDto): Pair<Map<String, String>, Map<String, String>> {
        val outcomeMap = mutableMapOf<String, String>()
        val titleMap = mutableMapOf<String, String>()

        event.markets.forEach { market ->
            val tokenIds = market.clobTokenIds
            val outcomes = market.outcomes
            val displayTitle = market.getTitleOrDefault(market.question)

            if (tokenIds != null && tokenIds.size == outcomes.size) {
                tokenIds.zip(outcomes).forEach { (tokenId, outcomeLabel) ->
                    outcomeMap[tokenId] = outcomeLabel // Map for color
                    titleMap[tokenId] = displayTitle    // Map for text
                }
            }
        }
        Log.d(TAG, "Built token maps. OutcomeMap: ${outcomeMap.size}, TitleMap: ${titleMap.size} for event ${event.id}")
        return Pair(outcomeMap, titleMap)
    }

    // --- Chart Loading --- //
    private fun loadChartData(timeRange: TimeRange) {
        val currentEvent = (uiState.value as? EventDetailUiState.Success)?.event ?: return
        Log.d(TAG, "Loading chart data for range: $timeRange")

        val topMarkets = currentEvent.markets
            .filter { it.yesPrice().let { price -> price != null && price > 0.0 } }
            .sortedByDescending { it.yesPrice() }
            .take(5)

        viewModelScope.launch {
            // --- Load data asynchronously ---
            val deferredResults = topMarkets.mapNotNull { market ->

                val tokenId = market.clobTokenIds?.firstOrNull()
                if (tokenId == null) {
                    Log.w(TAG, "No valid clobTokenId found for market ${market.id}")
                    null
                } else {
                    async { // Restore repository call
                        polymarketRepository.getMarketTimeseries(
                            marketTokenId = tokenId,
                            interval = timeRange.apiInterval,
                            resolutionInMinutes = timeRange.apiResolutionMins,
                        )
                    }
                }
            }

            // --- Process results --- //
            val results = deferredResults.awaitAll()

            val combinedMarketsResults = mutableListOf<InternalMarketTimeseries>()

            results.forEachIndexed { originalIndex, result: Result<List<TimeseriesPointDto>> ->
                result.onSuccess { pointsDto: List<TimeseriesPointDto> ->
                    val market = topMarkets[originalIndex]
                    Log.d(
                        TAG,
                        "Received ${pointsDto.size} points for market ${market.id}"
                    )

                    val chartEntries = pointsDto.map { point ->
                        point.close.let { price ->
                            // Convert timestamp (assumed seconds) to milliseconds for the chart's X-axis
                            val xValue = roundEpochToTimeRange(point.timestamp, timeRange) // Timestamp in seconds
                            val yValue = price.toFloat() * 100f
                            // Populate the map for the axis formatter, using milliseconds for consistency
                            xValue to yValue
                        }
                    }
                    if (chartEntries.isNotEmpty()) {
                        combinedMarketsResults.add(
                            InternalMarketTimeseries(
                                market = market,
                                yesPrice = market.yesPrice() ?: -1.0,
                                timeseries = chartEntries
                            )
                        )
                    }
                }.onFailure { error ->
                    val market = topMarkets[originalIndex]
                    Log.e(
                        TAG,
                        "Failed to load timeseries for market ${market.id}",
                        error
                    )
                }
            }

            // --- Update Vico Chart --- //
            if (!combinedMarketsResults.isEmpty()) {
                _isChartAvailable.value = true
                try {
                    chartModelProducer.runTransaction {
                        lineSeries {
                            combinedMarketsResults.forEach { x ->
                                series(
                                    x.timeseries.map { it.first },
                                    x.timeseries.map { it.second })
                            }
                        }
                        extras { extraStore ->
                            extraStore[LegendLabelKey] =
                                combinedMarketsResults.mapIndexed { index, value ->
                                    OrderedChartLabel(
                                        index,
                                        value.market.getChartLabel()
                                    )
                                }.toSet()
                        }
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Error during runTransaction with real data", e)
                }
            } else {
                _isChartAvailable.value = false
            }
        }
    }

    // --- Comments Loading (modifies flat _commentsState) --- //
    private fun loadCommentsInternal(reset: Boolean = false) {
        if (_commentsLoading.value || (!reset && !_canLoadMoreComments.value)) return

        _commentsLoading.value = true
        if (reset) {
            commentsOffset = 0
            _canLoadMoreComments.value = true
            _commentsError.value = null // Clear previous error on reset
            // Keep existing comments visible during refresh for better UX
        }

        viewModelScope.launch {
            val commentsParentEntityId = (_uiState.value as? EventDetailUiState.Success)?.event?.let { eventDto ->
                val firstSeries = eventDto.series?.firstOrNull()

                if(firstSeries != null) CommentsParentEntityId(CommentsParentEntityType.Series, firstSeries.id)
                else CommentsParentEntityId(CommentsParentEntityType.Event, eventDto.id)
            } ?: return@launch

            val result = polymarketRepository.getComments(
                parentEntity = commentsParentEntityId,
                limit = DEFAULT_COMMENTS_LIMIT,
                offset = commentsOffset,
                holdersOnly = _holdersOnly.value,
                order = _commentsSortOrder.value
                // order and ascending can be added if needed
            )

            result.onSuccess { comments ->
                val topLevelCommentsCount = comments.count { comment -> comment.parentCommentID == null }
                _canLoadMoreComments.value = topLevelCommentsCount == DEFAULT_COMMENTS_LIMIT
                _commentsState.value = if (reset) comments else _commentsState.value + comments
                commentsOffset = _commentsState.value.count { comment -> comment.parentCommentID == null }
                if (reset) _commentsError.value = null // Clear error on successful refresh
            }.onFailure {
                Log.e(TAG, "Failed to load comments for event $eventSlug", it)
                // Show error only if it's an initial load/refresh or list is empty
                if (reset || _commentsState.value.isEmpty()) {
                    _commentsError.value = it.message ?: "Failed to load comments"
                }
                _canLoadMoreComments.value = false // Stop pagination on error
            }
            _commentsLoading.value = false
        }
    }

    fun loadMoreComments() {
        loadCommentsInternal(reset = false)
    }

    fun refreshComments() {
        loadCommentsInternal(reset = true)
    }

    fun toggleHoldersOnly() {
        _holdersOnly.value = !_holdersOnly.value
        refreshComments() // Reload comments with the new filter setting
    }

    fun selectCommentsSortOrder(order: CommentsSortOrder) {
        if (order == _commentsSortOrder.value) return
        _commentsSortOrder.value = order
        refreshComments() // Reload comments with the new sort order
    }

    fun selectTimeRange(range: TimeRange) {
        if (range == _selectedTimeRange.value) return
        _selectedTimeRange.value = range
        loadChartData(range)
    }

    fun retryLoad() {
        // Decide whether to retry loading event details or just comments
        // If uiState is Error, retry loading everything
        if (_uiState.value is EventDetailUiState.Error) {
             loadEventDetailsAndInitialData()
        } else if (_commentsError.value != null) {
            // If only comments failed, retry refreshing comments
            refreshComments()
        }
    }

    private data class InternalMarketTimeseries(
        val market: MarketDto,
        val yesPrice: Double,
        val timeseries: List<Pair<Long, Float>>
    )

    // --- Helper function to process flat list into hierarchy --- //
    private fun processComments(flatList: List<CommentDto>): List<HierarchicalComment> {
        // Group replies by their parent ID
        val repliesMap = flatList
            .filter { it.parentCommentID != null }
            .groupBy { it.parentCommentID!! } // Group by non-null parent ID

        // Filter top-level comments
        val topLevelComments = flatList.filter { it.parentCommentID == null }

        // Map top-level comments to HierarchicalComment, attaching replies
        return topLevelComments.map { topLevel ->
            HierarchicalComment(
                comment = topLevel,
                // Find replies for this top-level comment, default to empty list
                replies = repliesMap[topLevel.id] ?: emptyList()
            )
        }
    }

    companion object {
        private const val TAG = "EventDetailViewModel"
    }
}

// UI state for event details screen
sealed interface EventDetailUiState {
    data object Loading : EventDetailUiState
    data class Success(val event: EventDto) : EventDetailUiState // Contains EventDto
    data class Error(val message: String) : EventDetailUiState
}

enum class TimeRange(val apiInterval: String, val apiResolutionMins: Int) {
    H1("1h", 5),
    H6("6h", 10),
    D1("1d", 15),
    W1("1w", 60),
    M1("1m", 60*4),
    ALL("max", 60*12)
}

private fun roundEpochToTimeRange(epochSeconds: Long, timeRange: TimeRange): Long {
    return roundEpochToMinutes(epochSeconds, timeRange.apiResolutionMins)
}

private fun roundEpochToMinutes(epochSeconds: Long, minutes: Int): Long {
    val interval = minutes * 60L
    val half = interval / 2
    return ((epochSeconds + half) / interval) * interval
}


private const val DEFAULT_COMMENTS_LIMIT = 40

internal val LegendLabelKey = ExtraStore.Key<Set<OrderedChartLabel>>()
internal data class OrderedChartLabel(val order: Int, val label: String)

// --- Data class for Hierarchical Comments --- //
data class HierarchicalComment(
    val comment: CommentDto,
    val replies: List<CommentDto> = emptyList()
)
