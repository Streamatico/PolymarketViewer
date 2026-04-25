package com.streamatico.polymarketviewer.ui.event_list

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.streamatico.polymarketviewer.data.model.clob_api.TimeseriesPointDto
import com.streamatico.polymarketviewer.data.model.data_api.LeaderBoardDto
import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTotalPositionValueDto
import com.streamatico.polymarketviewer.data.model.data_api.UserTradedDto
import com.streamatico.polymarketviewer.data.model.gamma_api.CommentDto
import com.streamatico.polymarketviewer.data.model.gamma_api.EventDto
import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PaginationDataDto
import com.streamatico.polymarketviewer.data.model.gamma_api.PaginationDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultDto
import com.streamatico.polymarketviewer.data.model.gamma_api.SearchResultOptimizedDto
import com.streamatico.polymarketviewer.data.model.gamma_api.TagDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import com.streamatico.polymarketviewer.data.model.gamma_api.demoEventDto
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import com.streamatico.polymarketviewer.data.preferences.WatchlistInteractor
import com.streamatico.polymarketviewer.domain.repository.CommentsParentEntityId
import com.streamatico.polymarketviewer.domain.repository.CommentsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketEventsSortOrder
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest {

    @Test
    fun `initial load publishes success state with fetched events`() = runViewModelTest {
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(successPage(events("event", 3)))
            tagsResult = Result.success(listOf(TagDto(id = "tag-1", label = "Crypto", slug = "crypto", forceShow = false)))
        }

        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals(listOf("event-0", "event-1", "event-2"), successState.events.map { it.id })
        assertEquals(POLYMARKET_EVENTS_SLUG_ALL, successState.tagSlug)
        assertEquals(PolymarketEventsSortOrder.DEFAULT_SORT_ORDER, successState.sortOrder)
        assertEquals(1, repository.eventRequests.size)
        assertEquals(0, repository.eventRequests.single().offset)
        assertFalse(repository.eventRequests.single().forceRefresh)
        assertEquals(1, viewModel.tagsState.value.size)
        assertFalse(viewModel.areTagsLoading.value)
        assertFalse(viewModel.canLoadMore.value)
    }

    @Test
    fun `watchlist tab with empty watchlist skips network and shows empty success`() = runViewModelTest {
        val repository = RecordingPolymarketRepository()
        val userPreferencesRepository = createUserPreferencesRepository().also {
            it.setWatchlistSelected(true)
        }
        val viewModel = createViewModel(
            repository = repository,
            userPreferencesRepository = userPreferencesRepository,
            watchlistInteractor = FakeWatchlistInteractor(emptySet()),
        )

        advanceUntilIdle()

        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertTrue(successState.events.isEmpty())
        assertEquals(POLYMARKET_EVENTS_SLUG_WATCHLIST, successState.tagSlug)
        assertTrue(repository.eventRequests.isEmpty())
        assertFalse(viewModel.canLoadMore.value)
        assertFalse(viewModel.isRefreshing.value)
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `refresh reloads first page with force refresh enabled`() = runViewModelTest {
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(successPage(events("initial", PAGE_SIZE_TEST)))
            enqueueEventsResult(successPage(events("refreshed", 3)))
        }
        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        viewModel.refreshEvents()
        advanceUntilIdle()

        assertEquals(2, repository.eventRequests.size)
        val refreshRequest = repository.eventRequests.last()
        assertEquals(0, refreshRequest.offset)
        assertTrue(refreshRequest.forceRefresh)

        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals(listOf("refreshed-0", "refreshed-1", "refreshed-2"), successState.events.map { it.id })
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun `pagination appends unique events and advances offset`() = runViewModelTest {
        val firstPage = events("page", PAGE_SIZE_TEST)
        val secondPage = listOf(event("page-19")) + events("page-next", PAGE_SIZE_TEST - 1)
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(successPage(firstPage))
            enqueueEventsResult(successPage(secondPage))
        }
        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        viewModel.loadMoreEvents()
        advanceUntilIdle()

        assertEquals(listOf(0, PAGE_SIZE_TEST), repository.eventRequests.map { it.offset })
        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals(PAGE_SIZE_TEST + PAGE_SIZE_TEST - 1, successState.events.size)
        assertEquals(1, successState.events.count { it.id == "page-19" })
        assertTrue(viewModel.canLoadMore.value)
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `pagination failure keeps current success items and disables further load more`() = runViewModelTest {
        val firstPage = events("page", PAGE_SIZE_TEST)
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(successPage(firstPage))
            enqueueEventsResult(Result.failure(IllegalStateException("Network error")))
        }
        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        viewModel.loadMoreEvents()
        advanceUntilIdle()

        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals(firstPage.map { it.id }, successState.events.map { it.id })
        assertFalse(viewModel.canLoadMore.value)
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `refresh cancels in flight pagination and replaces list with refreshed first page`() = runViewModelTest {
        val firstPage = events("page", PAGE_SIZE_TEST)
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(successPage(firstPage))
        }
        val pendingPagination = repository.enqueuePendingEventsCall()
        repository.enqueueEventsResult(successPage(events("refresh", 4)))
        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        viewModel.loadMoreEvents()
        runCurrent()
        val paginationRequest = pendingPagination.awaitStarted()
        assertEquals(PAGE_SIZE_TEST, paginationRequest.offset)
        assertTrue(viewModel.isLoadingMore.value)

        viewModel.refreshEvents()
        advanceUntilIdle()

        assertTrue(pendingPagination.cancelled)
        assertEquals(listOf(0, PAGE_SIZE_TEST, 0), repository.eventRequests.map { it.offset })
        assertTrue(repository.eventRequests.last().forceRefresh)
        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals(listOf("refresh-0", "refresh-1", "refresh-2", "refresh-3"), successState.events.map { it.id })
        assertFalse(viewModel.isRefreshing.value)
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `select tag cancels in flight initial load and shows events for new tag`() = runViewModelTest {
        val repository = RecordingPolymarketRepository()
        val pendingInitialLoad = repository.enqueuePendingEventsCall()
        repository.enqueueEventsResult(successPage(events("politics", 2)))

        val viewModel = createViewModel(repository = repository)
        runCurrent()
        val initialRequest = pendingInitialLoad.awaitStarted()
        assertEquals(null, initialRequest.tagSlug)

        viewModel.selectTag("politics")
        advanceUntilIdle()

        assertTrue(pendingInitialLoad.cancelled)
        assertEquals(listOf(null, "politics"), repository.eventRequests.map { it.tagSlug })
        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals("politics", successState.tagSlug)
        assertEquals(listOf("politics-0", "politics-1"), successState.events.map { it.id })
        assertEquals("politics", viewModel.selectedTagSlug.value)
    }

    @Test
    fun `initial DNS failure shows DNS settings action before any successful load in session`() = runViewModelTest {
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(Result.failure(java.net.UnknownHostException("gamma-api.polymarket.com")))
        }

        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        val errorState = assertInstanceOf(EventListUiState.Error::class.java, viewModel.uiState.value)
        assertTrue(errorState.showDnsSettings)
    }

    @Test
    fun `DNS failure after a successful load hides DNS settings action for current session`() = runViewModelTest {
        val repository = RecordingPolymarketRepository().apply {
            enqueueEventsResult(successPage(events("initial", 3)))
            enqueueEventsResult(Result.failure(java.net.UnknownHostException("gamma-api.polymarket.com")))
        }

        val viewModel = createViewModel(repository = repository)
        advanceUntilIdle()

        viewModel.refreshEvents()
        advanceUntilIdle()

        val errorState = assertInstanceOf(EventListUiState.Error::class.java, viewModel.uiState.value)
        assertFalse(errorState.showDnsSettings)
    }

    @Test
    fun `stale success from cancelled request does not overwrite newer sort selection`() = runViewModelTest {
        val repository = RecordingPolymarketRepository()
        val pendingInitialLoad = repository.enqueuePendingEventsCall(ignoreCancellation = true)
        repository.enqueueEventsResult(successPage(events("newest", 2)))

        val viewModel = createViewModel(repository = repository)
        runCurrent()
        pendingInitialLoad.awaitStarted()

        viewModel.selectSortOrder(PolymarketEventsSortOrder.NEWEST)
        advanceUntilIdle()

        pendingInitialLoad.complete(successPage(events("stale", 2)))
        advanceUntilIdle()

        assertTrue(pendingInitialLoad.cancelled)
        assertEquals(listOf(PolymarketEventsSortOrder.DEFAULT_SORT_ORDER, PolymarketEventsSortOrder.NEWEST), repository.eventRequests.map { it.order })
        assertEquals(listOf(null, listOf(POLYMARKET_GAMES_TAG_ID_FOR_TEST)), repository.eventRequests.map { it.excludeTagIds })
        val successState = assertInstanceOf(EventListUiState.Success::class.java, viewModel.uiState.value)
        assertEquals(PolymarketEventsSortOrder.NEWEST, successState.sortOrder)
        assertEquals(listOf("newest-0", "newest-1"), successState.events.map { it.id })
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun createViewModel(
        repository: RecordingPolymarketRepository,
        userPreferencesRepository: UserPreferencesRepository = createUserPreferencesRepository(),
        watchlistInteractor: WatchlistInteractor = FakeWatchlistInteractor(emptySet()),
    ): EventListViewModel {
        return EventListViewModel(
            polymarketRepository = repository,
            userPreferencesRepository = userPreferencesRepository,
            watchlistInteractor = watchlistInteractor,
        )
    }

    private fun createUserPreferencesRepository(): UserPreferencesRepository {
        return UserPreferencesRepository(InMemoryPreferencesDataStore())
    }

    private fun successPage(events: List<EventDto>): Result<PaginationDataDto<EventDto>> {
        return Result.success(
            PaginationDataDto(
                data = events,
                pagination = PaginationDto(
                    hasMore = events.size == PAGE_SIZE_TEST,
                    totalResults = events.size,
                ),
            )
        )
    }

    private fun events(prefix: String, count: Int): List<EventDto> {
        return (0 until count).map { index ->
            event(
                id = "$prefix-$index",
                featured = false,
                featuredOrder = null,
            )
        }
    }

    private fun event(
        id: String,
        featured: Boolean? = false,
        featuredOrder: Int? = null,
    ): EventDto {
        return demoEventDto(
            id = id,
            title = id,
            slug = id,
            rawMarkets = emptyList(),
            featured = featured,
            featuredOrder = featuredOrder,
        )
    }
}

private class FakeWatchlistInteractor(initialIds: Set<String>) : WatchlistInteractor {
    private val mutableWatchlistIds = MutableStateFlow(initialIds)

    override val watchlistIds: Flow<Set<String>> = mutableWatchlistIds.asStateFlow()

    override suspend fun toggleWatchlist(eventId: String): Boolean {
        mutableWatchlistIds.value = if (eventId in mutableWatchlistIds.value) {
            mutableWatchlistIds.value - eventId
        } else {
            mutableWatchlistIds.value + eventId
        }
        return true
    }
}

private class RecordingPolymarketRepository : PolymarketRepository {
    val eventRequests = mutableListOf<EventRequest>()
    var tagsResult: Result<List<TagDto>> = Result.success(emptyList())
    private val queuedEventBehaviors = ArrayDeque<EventCallBehavior>()

    fun enqueueEventsResult(result: Result<PaginationDataDto<EventDto>>) {
        queuedEventBehaviors.addLast(EventCallBehavior.Immediate(result))
    }

    fun enqueuePendingEventsCall(ignoreCancellation: Boolean = false): PendingEventsCall {
        return PendingEventsCall(ignoreCancellation).also {
            queuedEventBehaviors.addLast(EventCallBehavior.Pending(it))
        }
    }

    override suspend fun getEvents(
        limit: Int?,
        offset: Int?,
        active: Boolean?,
        tagSlug: String?,
        archived: Boolean?,
        closed: Boolean?,
        order: PolymarketEventsSortOrder,
        excludeTagIds: List<Long>?,
        ids: List<String>?,
        forceRefresh: Boolean,
    ): Result<PaginationDataDto<EventDto>> {
        val request = EventRequest(
            limit = limit,
            offset = offset,
            active = active,
            tagSlug = tagSlug,
            archived = archived,
            closed = closed,
            order = order,
            excludeTagIds = excludeTagIds,
            ids = ids,
            forceRefresh = forceRefresh,
        )
        eventRequests += request

        return when (val behavior = queuedEventBehaviors.removeFirstOrNull()) {
            is EventCallBehavior.Immediate -> behavior.result
            is EventCallBehavior.Pending -> behavior.call.awaitResult(request)
            null -> Result.failure(IllegalStateException("No queued events result"))
        }
    }

    override suspend fun getTags(): Result<List<TagDto>> = tagsResult

    override suspend fun getMarketDetails(marketId: String): Result<MarketDto> = unsupported()

    override suspend fun getEventDetailsBySlug(eventSlug: String): Result<EventDto> = unsupported()

    override suspend fun getMarketTimeseries(
        marketTokenId: String,
        interval: String,
        resolutionInMinutes: Int?,
        startTimestamp: Long?,
        endTimestamp: Long?,
    ): Result<List<TimeseriesPointDto>> = unsupported()

    override suspend fun getComments(
        parentEntity: CommentsParentEntityId,
        limit: Int?,
        offset: Int?,
        holdersOnly: Boolean,
        order: CommentsSortOrder,
    ): Result<List<CommentDto>> = unsupported()

    override suspend fun getUserProfile(userAddress: String): Result<UserProfileDto> = unsupported()

    override suspend fun searchPublicOptimized(
        query: String,
        limitPerType: Int,
        eventsStatus: String,
    ): Result<SearchResultOptimizedDto> = unsupported()

    override suspend fun searchPublicFull(
        query: String,
        limitPerType: Int,
        eventsStatus: String,
    ): Result<SearchResultDto> = unsupported()

    override suspend fun getPositions(
        userAddress: String,
        limit: Int,
        offset: Int,
    ): Result<List<UserPositionDto>> = unsupported()

    override suspend fun getClosedPositions(
        userAddress: String,
        limit: Int,
        offset: Int,
    ): Result<List<UserClosedPositionDto>> = unsupported()

    override suspend fun getTotalPositionsValue(
        userAddress: String,
        markets: List<String>?,
    ): Result<List<UserTotalPositionValueDto>> = unsupported()

    override suspend fun getUserLeaderBoard(userAddress: String): Result<LeaderBoardDto?> = unsupported()

    override suspend fun getUserTraded(userAddress: String): Result<UserTradedDto> = unsupported()

    override suspend fun getActivity(
        userAddress: String,
        limit: Int,
        offset: Int,
    ): Result<List<UserActivityDto>> = unsupported()

    private fun <T> unsupported(): Result<T> {
        return Result.failure(UnsupportedOperationException("Unused repository call in test"))
    }
}

private data class EventRequest(
    val limit: Int?,
    val offset: Int?,
    val active: Boolean?,
    val tagSlug: String?,
    val archived: Boolean?,
    val closed: Boolean?,
    val order: PolymarketEventsSortOrder,
    val excludeTagIds: List<Long>?,
    val ids: List<String>?,
    val forceRefresh: Boolean,
)

private sealed interface EventCallBehavior {
    data class Immediate(val result: Result<PaginationDataDto<EventDto>>) : EventCallBehavior
    data class Pending(val call: PendingEventsCall) : EventCallBehavior
}

private class PendingEventsCall(
    private val ignoreCancellation: Boolean = false,
) {
    private val startedRequest = CompletableDeferred<EventRequest>()
    private val completion = CompletableDeferred<Result<PaginationDataDto<EventDto>>>()

    var cancelled: Boolean = false
        private set

    suspend fun awaitStarted(): EventRequest = startedRequest.await()

    fun complete(result: Result<PaginationDataDto<EventDto>>) {
        completion.complete(result)
    }

    suspend fun awaitResult(request: EventRequest): Result<PaginationDataDto<EventDto>> {
        startedRequest.complete(request)
        val currentJob = currentCoroutineContext()[Job]
        currentJob?.invokeOnCompletion { cause: Throwable? ->
            if (cause is CancellationException) {
                cancelled = true
            }
        }

        return try {
            if (ignoreCancellation) {
                withContext(NonCancellable) { completion.await() }
            } else {
                completion.await()
            }
        } catch (exception: CancellationException) {
            cancelled = true
            throw exception
        }
    }
}

private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state.map { it }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.update { updated }
        return updated
    }
}

private const val PAGE_SIZE_TEST = 20
private const val POLYMARKET_GAMES_TAG_ID_FOR_TEST: Long = 100639



