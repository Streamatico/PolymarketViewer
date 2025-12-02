package com.streamatico.polymarketviewer.ui.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import com.streamatico.polymarketviewer.ui.navigation.NavKeys

import com.streamatico.polymarketviewer.ui.shared.PaginatedDataLoader
import com.streamatico.polymarketviewer.ui.shared.PaginatedList
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UserProfileState {
    data object Loading : UserProfileState
    data class Success(val userProfile: UserProfileDto) : UserProfileState
    data class Error(val message: String) : UserProfileState
}

@HiltViewModel(assistedFactory = UserProfileViewModel.Factory::class)
class UserProfileViewModel @AssistedInject constructor(
    private val repository: PolymarketRepository,
    @Assisted val navKey: NavKeys.UserProfile
) : ViewModel() {

    private val userAddress: String = navKey.userAddress

    @AssistedFactory
    interface Factory {
        fun create(navKey: NavKeys.UserProfile): UserProfileViewModel
    }

    // Separate state for profile header
    private val _profileState = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val profileState: StateFlow<UserProfileState> = _profileState.asStateFlow()

    private val _totalPositionsValue = MutableStateFlow<Double?>(null)
    val totalPositionsValue = _totalPositionsValue.asStateFlow()

    private val _userTraded = MutableStateFlow<Int?>(null)
    val userTraded = _userTraded.asStateFlow()

    // Paginated Lists exposed directly to UI
    val activePositions = PaginatedList(
        PaginatedDataLoader(
            scope = viewModelScope,
            fetchData = { offset -> repository.getPositions(userAddress, limit = 20, offset = offset) }
        )
    )

    val closedPositions = PaginatedList(
        PaginatedDataLoader(
            scope = viewModelScope,
            fetchData = { offset -> repository.getClosedPositions(userAddress, limit = 20, offset = offset) }
        )
    )

    val activities = PaginatedList(
        PaginatedDataLoader(
            scope = viewModelScope,
            fetchData = { offset -> repository.getActivity(userAddress, limit = 20, offset = offset) }
        )
    )

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = UserProfileState.Loading
            repository.getUserProfile(userAddress)
                .onSuccess { profile ->
                    _profileState.value = UserProfileState.Success(profile)
                }
                .onFailure {
                    _profileState.value = UserProfileState.Error(it.message ?: "Failed to load profile")
                }
        }
        viewModelScope.launch {
            repository.getTotalPositionsValue(userAddress)
                .onSuccess { result ->
                    _totalPositionsValue.value = result.sumOf { it.value ?: 0.0 }
                }
                .onFailure {
                    _totalPositionsValue.value = null
                }
        }
        viewModelScope.launch {
            repository.getUserTraded(userAddress)
                .onSuccess { result ->
                    _userTraded.value = result.traded
                }
                .onFailure {
                    _userTraded.value = null
                }
        }
    }

    fun retryLoadProfile() {
        loadUserProfile()
    }
}
