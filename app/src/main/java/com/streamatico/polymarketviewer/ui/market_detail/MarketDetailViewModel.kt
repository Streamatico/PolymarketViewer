package com.streamatico.polymarketviewer.ui.market_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketDetailViewModel @Inject constructor(
    private val polymarketRepository: PolymarketRepository,
    savedStateHandle: SavedStateHandle // To get marketId from navigation arguments
) : ViewModel() {

    private val marketId: String = checkNotNull(savedStateHandle[AppDestinations.MARKET_ID_ARG])

    private val _uiState = MutableStateFlow<MarketDetailUiState>(MarketDetailUiState.Loading)
    val uiState: StateFlow<MarketDetailUiState> = _uiState.asStateFlow()

    init {
        loadMarketDetails()
    }

    private fun loadMarketDetails() {
        _uiState.value = MarketDetailUiState.Loading
        viewModelScope.launch {
            polymarketRepository.getMarketDetails(marketId)
                .onSuccess { market ->
                    _uiState.value = MarketDetailUiState.Success(market)
                }
                .onFailure { throwable ->
                    _uiState.value = MarketDetailUiState.Error(throwable.message ?: "Unknown error loading details")
                }
        }
    }

    // Can add refresh function
    fun retryLoad() {
        loadMarketDetails()
    }
} 