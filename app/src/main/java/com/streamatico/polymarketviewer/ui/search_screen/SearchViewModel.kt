package com.streamatico.polymarketviewer.ui.search_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedEventDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel @AssistedInject constructor(
    private val polymarketRepository: PolymarketRepository
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(): SearchViewModel
    }

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(SEARCH_DEBOUNCE_MS)
                .map { it.trim() }
                .distinctUntilChanged()
                .collectLatest { query ->

                    if (query.isEmpty()) {
                        _uiState.value = SearchUiState.Empty
                        return@collectLatest
                    }

                    if (query.length < MIN_QUERY_LENGTH) {
                        return@collectLatest
                    }

                    _uiState.value = SearchUiState.Loading

                    performSearch(query)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState.Empty
    }

    private suspend fun performSearch(query: String) {
        _uiState.value = SearchUiState.Loading

        try {
            Log.d("SearchViewModel", "Searching for: $query")

            val result = polymarketRepository.searchPublicOptimized(
                query = query,
                limitPerType = 20,
                eventsStatus = "active"
            )

            result.onSuccess { searchResult ->
                val events = searchResult.events ?: emptyList()

                _uiState.value = SearchUiState.Success(
                    events = searchResult.events ?: emptyList(),
                )

                Log.d("SearchViewModel", "Found ${events.size} events")
            }

            result.onFailure { exception ->
                Log.e("SearchViewModel", "Search failed", exception)
                _uiState.value = SearchUiState.Error(
                    exception.message ?: "Failed to search events"
                )
            }
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Search error", e)
            _uiState.value = SearchUiState.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    fun retrySearch() {
        val currentQuery = _searchQuery.value.trim()
        if (currentQuery.isNotEmpty() && currentQuery.length >= MIN_QUERY_LENGTH) {
            _uiState.value = SearchUiState.Loading
            viewModelScope.launch {
                performSearch(currentQuery)
            }
        }
    }
}

private const val SEARCH_DEBOUNCE_MS = 1000L
private const val MIN_QUERY_LENGTH = 2

sealed interface SearchUiState {
    data object Empty : SearchUiState
    data object Loading : SearchUiState
    data class Success(val events: List<OptimizedEventDto>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
