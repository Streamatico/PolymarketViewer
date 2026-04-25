package com.streamatico.polymarketviewer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.preferences.DnsOverHttpsProvider
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    fun setDohEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDohEnabled(enabled)
        }
    }

    fun setDohProvider(provider: DnsOverHttpsProvider) {
        viewModelScope.launch {
            userPreferencesRepository.setDohProvider(provider)
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAnalyticsEnabled(enabled)
        }
    }
}

