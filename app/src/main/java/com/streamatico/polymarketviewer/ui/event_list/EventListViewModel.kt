package com.streamatico.polymarketviewer.ui.event_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import com.streamatico.polymarketviewer.data.preferences.WatchlistInteractor
import com.streamatico.polymarketviewer.ui.shared.UiError
import com.streamatico.polymarketviewer.ui.shared.toUiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class EventListViewModel(
    private val polymarketRepository: PolymarketRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val watchlistInteractor: WatchlistInteractor
) : ViewModel() {

    // --- Watchlist --- //
    val watchlistIds: StateFlow<Set<String>> = watchlistInteractor.watchlistIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // --- UI State --- //
    private val _uiState = MutableStateFlow<EventListUiState>(EventListUiState.Loading)
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    // --- Tags --- //
    private val _tagsState = MutableStateFlow<List<TagDto>>(emptyList())
    val tagsState: StateFlow<List<TagDto>> = _tagsState.asStateFlow()
    private val _selectedTagSlug = MutableStateFlow(POLYMARKET_EVENTS_SLUG_ALL)
    val selectedTagSlug: StateFlow<String> = _selectedTagSlug.asStateFlow()
    private val _areTagsLoading = MutableStateFlow(true)
    val areTagsLoading: StateFlow<Boolean> = _areTagsLoading.asStateFlow()

    // --- Sorting --- //
    private val _selectedSortOrder = MutableStateFlow(PolymarketEventsSortOrder.DEFAULT_SORT_ORDER) // Default sort
    val selectedSortOrder: StateFlow<PolymarketEventsSortOrder> = _selectedSortOrder.asStateFlow()

    // --- Pagination & Loading State --- //
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val _canLoadMore = MutableStateFlow(true)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()
    private val loadTracker = LoadTracker()

    init {
        viewModelScope.launch {
            val isWatchlistSelected = userPreferencesRepository.isWatchlistSelected.first()
            _selectedTagSlug.value = if (isWatchlistSelected) {
                POLYMARKET_EVENTS_SLUG_WATCHLIST
            } else {
                POLYMARKET_EVENTS_SLUG_ALL
            }
            if (isWatchlistSelected) {
                userPreferencesRepository.watchlistIds.first()
            }
            loadInitialTags()
            loadEventsInternal(LoadMode.INITIAL)
        }
    }

    private fun loadInitialTags() {
        viewModelScope.launch {
            _areTagsLoading.value = true
            polymarketRepository.getTags()
                .onSuccess { fetchedTags ->
                    _tagsState.value = fetchedTags
                    _areTagsLoading.value = false
                }
                .onFailure {
                    Log.e(TAG, "Failed to load initial tags", it)
                    _areTagsLoading.value = false
                }
        }
    }

    private fun loadEventsInternal(mode: LoadMode) {
        val loadContext = prepareLoad(mode) ?: return
        resetEventsForFreshLoadIfNeeded(mode)

        when (val queryPlan = buildQueryPlan(loadContext)) {
            QueryPlan.EmptyWatchlist -> handleEmptyWatchlist(loadContext)
            is QueryPlan.Network -> launchEventsLoad(loadContext, queryPlan.querySpec)
        }
    }

    fun refreshEvents() {
        Log.d(TAG, "Pull-to-refresh requested")
        loadEventsInternal(LoadMode.REFRESH)
    }

    fun loadMoreEvents() {
        loadEventsInternal(LoadMode.PAGINATION)
    }

    fun retryLoad() {
        loadInitialTags()
        loadEventsInternal(LoadMode.INITIAL)
    }

    fun selectTag(tagSlug: String) {
        if (_selectedTagSlug.value != tagSlug) {
            _selectedTagSlug.value = tagSlug
            viewModelScope.launch {
                userPreferencesRepository.setWatchlistSelected(tagSlug == POLYMARKET_EVENTS_SLUG_WATCHLIST)
            }
            loadEventsInternal(LoadMode.INITIAL)
        }
    }

    fun selectSortOrder(order: PolymarketEventsSortOrder) {
        if (_selectedSortOrder.value != order) {
            _selectedSortOrder.value = order
            loadEventsInternal(LoadMode.INITIAL)
        }
    }

    fun toggleWatchlist(eventId: String) {
        viewModelScope.launch {
            watchlistInteractor.toggleWatchlist(eventId)
            // Keep existing behavior: on watchlist tab we do not force a refresh after toggle.
        }
    }

    private fun prepareLoad(mode: LoadMode): LoadContext? {
        val selectedTag = _selectedTagSlug.value
        val selectedSortOrder = _selectedSortOrder.value
        val requestId: Long
        val offsetToLoad: Int

        when (mode) {
            LoadMode.REFRESH -> {
                requestId = reserveLoadRequestId()
                if (_isRefreshing.value || _isLoadingMore.value || loadTracker.activeJob?.isActive == true) {
                    cancelActiveLoad("manual refresh requested while another load is active")
                }
                offsetToLoad = 0
                _isRefreshing.value = true
            }

            LoadMode.PAGINATION -> {
                if (!_canLoadMore.value) {
                    Log.d(TAG, "Skipping pagination because there is no more data to load")
                    return null
                }
                if (_isLoadingMore.value || _isRefreshing.value || loadTracker.activeJob?.isActive == true) {
                    Log.d(
                        TAG,
                        "Skipping pagination because another load is active: refreshing=${_isRefreshing.value}, loadingMore=${_isLoadingMore.value}, jobActive=${loadTracker.activeJob?.isActive == true}"
                    )
                    return null
                }
                requestId = reserveLoadRequestId()
                offsetToLoad = loadTracker.currentOffset
                _isLoadingMore.value = true
            }

            LoadMode.INITIAL -> {
                requestId = reserveLoadRequestId()
                if (_isRefreshing.value || _isLoadingMore.value || loadTracker.activeJob?.isActive == true) {
                    cancelActiveLoad("initial load requested while another load is active")
                }
                offsetToLoad = 0
                if (currentVisibleEvents().isEmpty()) {
                    _uiState.value = EventListUiState.Loading
                }
            }
        }

        return LoadContext(
            mode = mode,
            requestId = requestId,
            selectedTag = selectedTag,
            selectedSortOrder = selectedSortOrder,
            offset = offsetToLoad,
        )
    }

    private fun resetEventsForFreshLoadIfNeeded(mode: LoadMode) {
        if (!mode.resetsList) return

        loadTracker.currentOffset = 0
        _canLoadMore.value = true
    }

    private fun buildQueryPlan(loadContext: LoadContext): QueryPlan {
        if (loadContext.selectedTag == POLYMARKET_EVENTS_SLUG_WATCHLIST) {
            val watchlistEventIds = watchlistIds.value.toList()
            if (watchlistEventIds.isEmpty()) {
                return QueryPlan.EmptyWatchlist
            }

            return QueryPlan.Network(
                EventQuerySpec(
                    active = null,
                    offset = loadContext.offset,
                    tagSlug = null,
                    archived = null,
                    closed = null,
                    order = loadContext.selectedSortOrder,
                    excludeTagIds = excludeTagIdsFor(loadContext.selectedSortOrder),
                    ids = watchlistEventIds,
                    forceRefresh = loadContext.mode == LoadMode.REFRESH,
                )
            )
        }

        return QueryPlan.Network(
            EventQuerySpec(
                active = true,
                offset = loadContext.offset,
                tagSlug = loadContext.selectedTag.takeUnless { it == POLYMARKET_EVENTS_SLUG_ALL },
                archived = false,
                closed = false,
                order = loadContext.selectedSortOrder,
                excludeTagIds = excludeTagIdsFor(loadContext.selectedSortOrder),
                ids = null,
                forceRefresh = loadContext.mode == LoadMode.REFRESH,
            )
        )
    }

    private fun handleEmptyWatchlist(loadContext: LoadContext) {
        Log.d(TAG, "Skipping network load because watchlist is empty")
        _uiState.value = EventListUiState.Success(emptyList(), loadContext.selectedTag, loadContext.selectedSortOrder)
        _isRefreshing.value = false
        _isLoadingMore.value = false
        _canLoadMore.value = false
        loadTracker.activeJob = null
        loadTracker.currentOffset = 0
    }

    private fun launchEventsLoad(
        loadContext: LoadContext,
        querySpec: EventQuerySpec,
    ) {
        loadTracker.activeJob = viewModelScope.launch {
            try {
                Log.d(
                    TAG,
                    "Loading events with requestId=${loadContext.requestId}, mode=${loadContext.mode.logName}, order=${loadContext.selectedSortOrder}, tag=${loadContext.selectedTag}, offset=${loadContext.offset}"
                )
                val result = polymarketRepository.getEvents(
                    active = querySpec.active,
                    limit = PAGE_SIZE,
                    offset = querySpec.offset,
                    tagSlug = querySpec.tagSlug,
                    archived = querySpec.archived,
                    closed = querySpec.closed,
                    order = querySpec.order,
                    excludeTagIds = querySpec.excludeTagIds,
                    ids = querySpec.ids,
                    forceRefresh = querySpec.forceRefresh,
                )

                result.onSuccess { newEvents ->
                    if (!isLatestRequest(loadContext.requestId)) {
                        Log.d(TAG, "Ignoring stale success result for requestId=${loadContext.requestId}")
                        return@onSuccess
                    }

                    handleEventsSuccess(loadContext, newEvents.data)
                }
                result.onFailure { throwable ->
                    if (!isLatestRequest(loadContext.requestId)) {
                        Log.d(TAG, "Ignoring stale failure result for requestId=${loadContext.requestId}", throwable)
                        return@onFailure
                    }

                    handleEventsFailure(loadContext, throwable)
                }
            } finally {
                finishLoad(loadContext)
            }
        }
    }

    private fun handleEventsSuccess(
        loadContext: LoadContext,
        fetchedEvents: List<EventDto>,
    ) {
        _canLoadMore.value = fetchedEvents.size == PAGE_SIZE
        val combinedEvents = if (loadContext.mode == LoadMode.PAGINATION) {
            currentVisibleEvents() + fetchedEvents
        } else {
            fetchedEvents
        }
        val uniqueEvents = combinedEvents.distinctBy { it.id }
        loadTracker.currentOffset = uniqueEvents.size
        publishSuccess(uniqueEvents, loadContext.selectedTag, loadContext.selectedSortOrder)
    }

    private fun handleEventsFailure(
        loadContext: LoadContext,
        throwable: Throwable,
    ) {
        Log.e(TAG, "Failed to load events", throwable)
        if (loadContext.mode == LoadMode.PAGINATION) {
            _canLoadMore.value = false
        }

        val visibleEvents = currentVisibleEvents()
        if (loadContext.mode != LoadMode.PAGINATION || visibleEvents.isEmpty()) {
            _uiState.value = EventListUiState.Error(
                throwable.toUiError(title = "Failed to load events")
            )
            return
        }

        publishSuccess(visibleEvents, loadContext.selectedTag, loadContext.selectedSortOrder)
    }

    private fun finishLoad(loadContext: LoadContext) {
        if (!isLatestRequest(loadContext.requestId)) return

        when (loadContext.mode) {
            LoadMode.REFRESH -> _isRefreshing.value = false
            LoadMode.PAGINATION -> _isLoadingMore.value = false
            LoadMode.INITIAL -> Unit
        }

        if (loadContext.mode == LoadMode.INITIAL && _uiState.value is EventListUiState.Loading) {
            val visibleEvents = currentVisibleEvents()
            if (visibleEvents.isNotEmpty()) {
                publishSuccess(visibleEvents, loadContext.selectedTag, loadContext.selectedSortOrder)
            }
        }

        loadTracker.activeJob = null
    }

    private fun excludeTagIdsFor(sortOrder: PolymarketEventsSortOrder): List<Long>? {
        return if (sortOrder == PolymarketEventsSortOrder.NEWEST) {
            listOf(POLYMARKET_GAMES_TAG_ID)
        } else {
            null
        }
    }

    private fun currentVisibleEvents(): List<EventDto> {
        return (uiState.value as? EventListUiState.Success)?.events.orEmpty()
    }

    private fun publishSuccess(
        events: List<EventDto>,
        tagSlug: String,
        sortOrder: PolymarketEventsSortOrder,
    ) {
        val displayEvents = if (sortOrder == PolymarketEventsSortOrder.DEFAULT_SORT_ORDER) {
            events.sortedWith(
                compareBy { if (it.featured == true) it.featuredOrder else 1000 }
            )
        } else {
            events
        }

        _uiState.value = EventListUiState.Success(
            events = displayEvents,
            tagSlug = tagSlug,
            sortOrder = sortOrder,
        )
    }

    private fun isLatestRequest(requestId: Long): Boolean = requestId == loadTracker.activeRequestId

    private fun cancelActiveLoad(reason: String) {
        if (loadTracker.activeJob?.isActive == true) {
            Log.d(TAG, "Cancelling active events load: $reason")
            loadTracker.activeJob?.cancel()
        }
        _isRefreshing.value = false
        _isLoadingMore.value = false
    }

    private fun reserveLoadRequestId(): Long {
        loadTracker.activeRequestId += 1
        return loadTracker.activeRequestId
    }
}

private enum class LoadMode(
    val logName: String,
    val resetsList: Boolean,
) {
    INITIAL(logName = "initial-load", resetsList = true),
    REFRESH(logName = "manual-refresh", resetsList = true),
    PAGINATION(logName = "pagination", resetsList = false),
}

private data class LoadContext(
    val mode: LoadMode,
    val requestId: Long,
    val selectedTag: String,
    val selectedSortOrder: PolymarketEventsSortOrder,
    val offset: Int,
)

private data class EventQuerySpec(
    val active: Boolean?,
    val offset: Int,
    val tagSlug: String?,
    val archived: Boolean?,
    val closed: Boolean?,
    val order: PolymarketEventsSortOrder,
    val excludeTagIds: List<Long>?,
    val ids: List<String>?,
    val forceRefresh: Boolean,
)

private sealed interface QueryPlan {
    data object EmptyWatchlist : QueryPlan
    data class Network(val querySpec: EventQuerySpec) : QueryPlan
}

private class LoadTracker {
    var currentOffset: Int = 0
    var activeJob: Job? = null
    var activeRequestId: Long = 0L
}

sealed interface EventListUiState {
    object Loading : EventListUiState
    data class Success(
        val events: List<EventDto>,
        val tagSlug: String = POLYMARKET_EVENTS_SLUG_ALL,
        val sortOrder: PolymarketEventsSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER
    ) : EventListUiState
    data class Error(val error: UiError) : EventListUiState
}

private const val PAGE_SIZE = 20
private const val TAG = "EventListViewModel"

const val POLYMARKET_EVENTS_SLUG_ALL = "all"
const val POLYMARKET_EVENTS_SLUG_WATCHLIST = "watchlist"

private const val POLYMARKET_GAMES_TAG_ID: Long = 100639