package com.streamatico.polymarketviewer.ui.about

import androidx.lifecycle.ViewModel
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val userPreferencesRepository: UserPreferencesRepository
) : ViewModel()

