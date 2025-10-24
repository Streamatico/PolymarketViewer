package com.streamatico.polymarketviewer.ui.search_screen

import androidx.lifecycle.ViewModel
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val polymarketRepository: PolymarketRepository
): ViewModel() {
    // TODO: Implement search logic with polymarketRepository.searchPublicOptimized()
}
