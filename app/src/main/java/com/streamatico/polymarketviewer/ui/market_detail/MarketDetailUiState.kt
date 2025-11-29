package com.streamatico.polymarketviewer.ui.market_detail

import com.streamatico.polymarketviewer.data.model.gamma_api.MarketDto

/**
 * Represents UI state for the market details screen.
 */
sealed interface MarketDetailUiState {
    object Loading : MarketDetailUiState
    data class Success(val market: MarketDto) : MarketDetailUiState
    data class Error(val message: String) : MarketDetailUiState
}