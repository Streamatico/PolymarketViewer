package com.streamatico.polymarketviewer.ui.user_profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.model.UserProfileDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.navigation.AppDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState
    data class Success(val userProfile: UserProfileDto) : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: PolymarketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get user address from navigation arguments
    private val userAddress: String = checkNotNull(savedStateHandle[AppDestinations.USER_ADDRESS_ARG]) // Use your actual arg name

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            val result = repository.getUserProfile(userAddress)
            _uiState.value = result.fold(
                onSuccess = { UserProfileUiState.Success(it) },
                onFailure = { UserProfileUiState.Error(it.message ?: "Failed to load profile") }
            )
        }
    }

    fun retryLoad() {
        loadUserProfile()
    }
}