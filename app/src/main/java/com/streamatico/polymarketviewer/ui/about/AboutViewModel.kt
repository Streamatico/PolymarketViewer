package com.streamatico.polymarketviewer.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AboutViewModel.Factory::class)
class AboutViewModel @AssistedInject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(): AboutViewModel
    }

    val userPreferencesFlow = userPreferencesRepository
        .userPreferencesFlow

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAnalyticsEnabled(enabled)
        }
    }
}

