package com.streamatico.polymarketviewer.ui.event_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class EventListViewModel(
    private val polymarketRepository: PolymarketRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- Watchlist --- //
    val watchlistIds: StateFlow<Set<String>> = userPreferencesRepository.watchlistIds
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
    private val _eventList = MutableStateFlow<List<EventDto>>(emptyList())
    private var currentOffset = 0

    init {
        loadInitialTags()
        loadEventsInternal(isInitialLoad = true)
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
                    Log.e("EventListViewModel", "Failed to load initial tags", it)
                    _areTagsLoading.value = false
                }
        }
    }

    private fun loadEventsInternal(
        isInitialLoad: Boolean = false,
        isManualRefresh: Boolean = false,
        isPagination: Boolean = false
    ) {
        val currentTag = _selectedTagSlug.value
        val currentOrder = _selectedSortOrder.value // Get current sort order
        //val isAscending = currentOrder == "endDate" // Check if ascending needed
        val offsetToLoad: Int

        when {
            isManualRefresh -> {
                if (_isRefreshing.value || _isLoadingMore.value) return
                offsetToLoad = 0
                _isRefreshing.value = true
            }
            isPagination -> {
                if (!_canLoadMore.value || _isLoadingMore.value || _isRefreshing.value) return
                offsetToLoad = currentOffset
                _isLoadingMore.value = true
            }
            isInitialLoad -> {
                // Don't reset loading if only sort/tag changed while already showing data
                if (_isRefreshing.value || _isLoadingMore.value) return
                offsetToLoad = 0
                if (_eventList.value.isEmpty()) {
                    _uiState.value = EventListUiState.Loading
                }
            }
            else -> return
        }

        // Reset list only on initial load or manual refresh
        if (isInitialLoad || isManualRefresh) {
            currentOffset = 0
            _canLoadMore.value = true
            _eventList.value = emptyList()
        }

        // Determine parameters for repository call
        val idList: List<String>?
        val reqTagSlug: String?
        val reqActive: Boolean?
        val reqClosed: Boolean?
        val reqArchived: Boolean?

        if (currentTag == POLYMARKET_EVENTS_SLUG_WATCHLIST) {
            val ids = watchlistIds.value.toList()
            if (ids.isEmpty()) {
                // If watchlist is empty, skip network call and show empty success state
                _uiState.value = EventListUiState.Success(emptyList(), currentTag, currentOrder)
                _isRefreshing.value = false
                _isLoadingMore.value = false
                _canLoadMore.value = false
                return
            }
            idList = ids
            reqTagSlug = null
            reqActive = null // Fetch all statuses for watchlist
            reqClosed = null
            reqArchived = null
        } else {
            idList = null
            reqTagSlug = if (currentTag == POLYMARKET_EVENTS_SLUG_ALL) null else currentTag
            reqActive = true
            reqClosed = false
            reqArchived = false
        }

        val excludeTagIds = if(currentOrder == PolymarketEventsSortOrder.NEWEST) {
            listOf(POLYMARKET_GAMES_TAG_ID)
        } else {
            null
        }

        viewModelScope.launch {
            try {
                Log.d("EventListViewModel", "Loading events with order: $currentOrder, tag: $currentTag, offset: $offsetToLoad")
                val result = polymarketRepository.getEvents(
                    active = reqActive,
                    limit = PAGE_SIZE,
                    offset = offsetToLoad,
                    tagSlug = reqTagSlug,
                    archived = reqArchived,
                    closed = reqClosed,
                    order = currentOrder,
                    excludeTagIds = excludeTagIds,
                    ids = idList
                )

                result.onSuccess { newEvents ->
                    _canLoadMore.value = newEvents.data.size == PAGE_SIZE
                    val combinedList: List<EventDto> = if (isPagination) _eventList.value + newEvents.data else newEvents.data
                    // Ensure uniqueness before updating the main list and UI state
                    val uniqueList = combinedList.distinctBy { it.id }
                    _eventList.value = uniqueList
                    currentOffset = uniqueList.size // Update offset based on unique list size

                    // Keep client-side featured sorting for now, might need adjustment
                    val sortedList = uniqueList
                        .let { events ->
                            if(currentOrder == PolymarketEventsSortOrder.DEFAULT_SORT_ORDER) {
                                // Sort by featured order first, then by default order
                                events.sortedWith(
                                    compareBy { if(it.featured == true) it.featuredOrder else 1000}
                                )
                            } else {
                                events
                            }
                        }

                    _uiState.value = EventListUiState.Success(sortedList, currentTag, currentOrder)
                }
                result.onFailure { exception ->
                    Log.e("EventListViewModel", "Failed to load events", exception)
                    if (isPagination) _canLoadMore.value = false
                    // Assign existing unique list on error if not empty
                    val currentUniqueList = _eventList.value // Already unique
                    if ((isInitialLoad || isManualRefresh || currentUniqueList.isEmpty())) {
                        _uiState.value = EventListUiState.Error(exception.message ?: "Error loading events")
                    } else {
                        _uiState.value = EventListUiState.Success(currentUniqueList, currentTag, currentOrder) // Show existing data on pagination error
                    }
                }
            } finally {
                if (isManualRefresh) _isRefreshing.value = false
                if (isPagination) _isLoadingMore.value = false
                // Ensure loading state is cleared if it was set initially
                if (isInitialLoad && _uiState.value is EventListUiState.Loading) {
                   if (_eventList.value.isNotEmpty()) {
                       _uiState.value = EventListUiState.Success(_eventList.value, currentTag, currentOrder)
                   } // Error state is handled in onFailure
                }
            }
        }
    }

    fun refreshEvents() {
        loadEventsInternal(isManualRefresh = true)
    }

    fun loadMoreEvents() {
        loadEventsInternal(isPagination = true)
    }

    fun retryLoad() {
        loadInitialTags()
        loadEventsInternal(isInitialLoad = true)
    }

    fun selectTag(tagSlug: String) {
        if (_selectedTagSlug.value != tagSlug) {
            _selectedTagSlug.value = tagSlug
            loadEventsInternal(isInitialLoad = true) // Trigger initial load for the new tag
        }
    }

    fun selectSortOrder(order: PolymarketEventsSortOrder) {
        if (_selectedSortOrder.value != order) {
            _selectedSortOrder.value = order
            loadEventsInternal(isInitialLoad = true) // Trigger initial load with new sort
        }
    }

    fun toggleWatchlist(eventId: String) {
        viewModelScope.launch {
            val success = userPreferencesRepository.toggleWatchlist(eventId)
            if (!success) {
                // TODO: Emit side effect to show error message (e.g., Snackbar)
                // For now, we just silently ignore or could log it
                Log.w("EventListViewModel", "Watchlist limit reached")
            }
            // If we are currently on the Watchlist tab, we might want to refresh the list
            // However, removing an item while viewing it might be jarring.
            // For now, let's keep it simple. If we are on watchlist and remove it, it won't disappear until refresh.
            // But if we want it to be reactive, we should observe watchlistIds in loadEventsInternal or trigger reload.
            if (_selectedTagSlug.value == POLYMARKET_EVENTS_SLUG_WATCHLIST) {
                // Optional: trigger refresh to remove it from view
                // loadEventsInternal(isManualRefresh = true)
            }
        }
    }
}

sealed interface EventListUiState {
    object Loading : EventListUiState
    data class Success(
        val events: List<EventDto>,
        val tagSlug: String = POLYMARKET_EVENTS_SLUG_ALL,
        val sortOrder: PolymarketEventsSortOrder = PolymarketEventsSortOrder.DEFAULT_SORT_ORDER
    ) : EventListUiState
    data class Error(val message: String) : EventListUiState
}

private const val PAGE_SIZE = 20

const val POLYMARKET_EVENTS_SLUG_ALL = "all"
const val POLYMARKET_EVENTS_SLUG_WATCHLIST = "watchlist"

private const val POLYMARKET_GAMES_TAG_ID: Long = 100639