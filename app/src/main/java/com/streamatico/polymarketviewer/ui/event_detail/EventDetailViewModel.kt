package com.streamatico.polymarketviewer.ui.event_detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.streamatico.polymarketviewer.data.model.EventDto
import com.streamatico.polymarketviewer.data.model.MarketDto
import com.streamatico.polymarketviewer.data.model.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.CommentDto
import com.streamatico.polymarketviewer.data.model.getTitleOrDefault
import com.streamatico.polymarketviewer.data.model.yesPrice
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.serialization.json.Json
import javax.inject.Inject

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

// --- Label for timeline data (dates) --- //
// val xToDateMapKey = ExtraStore.Key<Map<Float, Long>>()

//private const val CHART_UPDATE_INTERVAL_MINUTES = 15
private const val DEFAULT_COMMENTS_LIMIT = 40

internal val LegendLabelKey = ExtraStore.Key<Set<OrderedChartLabel>>()
internal data class OrderedChartLabel(val order: Int, val label: String)

// --- Data class for Hierarchical Comments --- //
data class HierarchicalComment(
    val comment: CommentDto,
    val replies: List<CommentDto> = emptyList()
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val polymarketRepository: PolymarketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get event ID from navigation arguments
    private val eventId: String = checkNotNull(savedStateHandle[AppDestinations.EVENT_ID_ARG])

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
    // val commentsState: StateFlow<List<CommentDto>> = _commentsState.asStateFlow() // Keep private if only hierarchical is exposed

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

    private val _canLoadMoreComments = MutableStateFlow(true)
    val canLoadMoreComments: StateFlow<Boolean> = _canLoadMoreComments.asStateFlow()
    private var commentsOffset = 0

    // Configure Json parser once
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    init {
        loadEventDetailsAndInitialData()
    }

    private fun loadEventDetailsAndInitialData() {
        viewModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            _eventOutcomeTokensMap.value = emptyMap() // Reset maps
            _eventTokenToGroupTitleMap.value = emptyMap()
            val eventResult = polymarketRepository.getEventDetails(eventId)

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
            try {
                // Use Kotlinx Serialization
                val tokenIds: List<String>? = market.clobTokenIds?.let { jsonParser.decodeFromString<List<String>>(it) }
                val outcomes: List<String>? = market.outcomesJson?.let { jsonParser.decodeFromString<List<String>>(it) }
                val displayTitle = market.getTitleOrDefault(market.question)

                if (tokenIds != null && outcomes != null && tokenIds.size == outcomes.size) {
                    tokenIds.zip(outcomes).forEach { (tokenId, outcomeLabel) ->
                        outcomeMap[tokenId] = outcomeLabel // Map for color
                        titleMap[tokenId] = displayTitle    // Map for text
                    }
                }
            } catch (e: Exception) {
                Log.e("EventDetailViewModel", "Failed to parse token/outcome JSON for market ${market.id}", e)
            }
        }
        Log.d("EventDetailViewModel", "Built token maps. OutcomeMap: ${outcomeMap.size}, TitleMap: ${titleMap.size} for event ${event.id}")
        return Pair(outcomeMap, titleMap)
    }

    // --- Chart Loading --- //
    private fun loadChartData(range: TimeRange) {
        val currentEvent = (uiState.value as? EventDetailUiState.Success)?.event ?: return
        Log.d("EventDetailVM", "Loading chart data for range: $range")

        val topMarkets = currentEvent.markets
            .filter { it.yesPrice().let { price -> price != null && price > 0.0 } }
            .sortedByDescending { it.yesPrice() }
            .take(5)

        viewModelScope.launch {
            // --- Load data asynchronously ---
            val deferredResults = topMarkets.mapNotNull { market ->
                val tokenIds: List<String>? = try {
                    // Use Kotlinx Serialization
                    market.clobTokenIds?.let { jsonParser.decodeFromString<List<String>>(it) }
                } catch (e: Exception) {
                    Log.e(
                        "EventDetailVM",
                        "Failed to parse clobTokenIds for market ${market.id}: ${market.clobTokenIds}",
                        e
                    )
                    null
                }
                val tokenId = tokenIds?.firstOrNull()
                if (tokenId == null) {
                    Log.w("EventDetailVM", "No valid clobTokenId found for market ${market.id}")
                    null
                } else {
                    //val timeseriesParams = getTimeSeriesInterval(range)
                    async { // Restore repository call
                        polymarketRepository.getMarketTimeseries(
                            marketTokenId = tokenId,
                            interval = range.apiInterval,
                            resolutionInMinutes = range.apiResolutionMins
                        )
                    }
                }
            }

            // --- Process results --- //
            val results = deferredResults.awaitAll()

            //val allChartPoints = mutableMapOf<Float, Long>() // Map: X-axis value (timestamp as Float) -> Original timestamp (Long)
            val combinedMarketsResults = mutableListOf<InternalMarketTimeseries>()

            results.forEachIndexed { originalIndex, result: Result<List<TimeseriesPointDto>> ->
                result.onSuccess { pointsDto: List<TimeseriesPointDto> ->
                    val market = topMarkets[originalIndex]
                    Log.d(
                        "EventDetailVM",
                        "Received ${pointsDto.size} points for market ${market.id}"
                    )

                    val chartEntries = pointsDto.map { point ->
                        point.close.let { price ->
                            // Convert timestamp (assumed seconds) to milliseconds for the chart's X-axis
                            val xValue = point.timestamp // Timestamp in seconds
                            val yValue = price.toFloat() * 100f
                            // Populate the map for the axis formatter, using milliseconds for consistency
                            //allChartPoints[xValue] = timestampMillis
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
                        "EventDetailVM",
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
                    //Log.d("EventDetailVM", "Real chart data updated. X-axis map size: ${allChartPoints.size}")
                } catch (e: Throwable) {
                    Log.e("EventDetailVM", "Error during runTransaction with real data", e)
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
            // Keep existing comments visible during refresh? Or clear?
             _commentsState.value = emptyList() // Clear list on refresh
        }

        viewModelScope.launch {
            val result = polymarketRepository.getComments(
                eventId = eventId,
                limit = DEFAULT_COMMENTS_LIMIT,
                offset = commentsOffset,
                holdersOnly = _holdersOnly.value.takeIf { it } // Pass true only if true, null otherwise
                // order and ascending can be added if needed
            )

            result.onSuccess {
                _canLoadMoreComments.value = it.size == DEFAULT_COMMENTS_LIMIT
                _commentsState.value = if (reset) it else _commentsState.value + it
                commentsOffset = _commentsState.value.size
                if (reset) _commentsError.value = null // Clear error on successful refresh
            }.onFailure {
                Log.e("EventDetailViewModel", "Failed to load comments for event $eventId", it)
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
        } // Consider sorting topLevelComments by createdAt if needed
    }
}