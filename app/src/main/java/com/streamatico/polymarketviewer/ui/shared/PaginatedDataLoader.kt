package com.streamatico.polymarketviewer.ui.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaginatedUiState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val canLoadMore: Boolean = true,
    val error: String? = null
)

class PaginatedDataLoader<T>(
    private val scope: CoroutineScope,
    private val fetchData: suspend (offset: Int) -> Result<List<T>>,
    private val limit: Int = 20
) {
    private val _state = MutableStateFlow(PaginatedUiState<T>())
    val state: StateFlow<PaginatedUiState<T>> = _state.asStateFlow()

    private var currentOffset = 0

    fun loadMore(reset: Boolean = false) {
        val currentState = _state.value
        if ((!currentState.canLoadMore && !reset) || currentState.isLoading) return

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            if (reset) {
                currentOffset = 0
            }

            fetchData(currentOffset).fold(
                onSuccess = { newItems ->
                    _state.update { state ->
                        val updatedItems = if (reset) newItems else state.items + newItems
                        val canLoadMore = newItems.size >= limit
                        currentOffset += newItems.size
                        
                        state.copy(
                            items = updatedItems,
                            isLoading = false,
                            canLoadMore = canLoadMore
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }
    
    fun loadIfNeeded() {
        if (_state.value.items.isEmpty() && _state.value.canLoadMore && !_state.value.isLoading) {
            loadMore()
        }
    }
}

/**
 * A State Holder for a paginated list that exposes both state and actions.
 * This class is intended to be exposed by ViewModels to the UI layer.
 */
class PaginatedList<T>(
    private val loader: PaginatedDataLoader<T>
) {
    val state: StateFlow<PaginatedUiState<T>> = loader.state

    fun loadMore() = loader.loadMore()
    fun refresh() = loader.loadMore(reset = true)
    fun loadIfNeeded() = loader.loadIfNeeded()
}
