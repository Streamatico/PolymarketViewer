package com.streamatico.polymarketviewer.ui.market_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.navigation.NavKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketDetailViewModel(
    private val polymarketRepository: PolymarketRepository,
    navKey: NavKeys.MarketDetail
) : ViewModel() {

    private val marketId: String = navKey.marketId

    private val _uiState = MutableStateFlow<MarketDetailUiState>(MarketDetailUiState.Loading)
    val uiState: StateFlow<MarketDetailUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadMarketDetails()
    }

    private fun loadMarketDetails(isManualRefresh: Boolean = false) {
        if (isManualRefresh && _isRefreshing.value) return

        viewModelScope.launch {
            if (isManualRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = MarketDetailUiState.Loading
            }

            polymarketRepository.getMarketDetails(marketId)
                .onSuccess { market ->
                    _uiState.value = MarketDetailUiState.Success(market)
                }
                .onFailure { throwable ->
                    if (isManualRefresh && _uiState.value is MarketDetailUiState.Success) {
                        Log.e(TAG, "Failed to refresh market details for market $marketId", throwable)
                    } else {
                        _uiState.value = MarketDetailUiState.Error(throwable.message ?: "Unknown error loading details")
                    }
                }

            if (isManualRefresh) {
                _isRefreshing.value = false
            }
        }
    }

    fun retryLoad() {
        loadMarketDetails()
    }

    fun refreshMarketDetails() {
        loadMarketDetails(isManualRefresh = true)
    }

    companion object {
        private const val TAG = "MarketDetailViewModel"
    }
}