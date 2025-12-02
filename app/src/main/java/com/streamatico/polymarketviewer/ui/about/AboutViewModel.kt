package com.streamatico.polymarketviewer.ui.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository

import kotlinx.coroutines.launch


class AboutViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {


    val userPreferencesFlow = userPreferencesRepository
        .userPreferencesFlow

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAnalyticsEnabled(enabled)
        }
    }
}

