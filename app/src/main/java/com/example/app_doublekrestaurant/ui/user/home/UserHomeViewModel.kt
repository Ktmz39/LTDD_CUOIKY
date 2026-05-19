package com.example.app_doublekrestaurant.ui.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserHomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val featuredItems: List<FoodItem> = emptyList(),
    val popularItems: List<FoodItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserHomeUiState(isLoading = true))
    val uiState: StateFlow<UserHomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadMenuItems()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    private fun loadMenuItems() {
        viewModelScope.launch {
            restaurantRepository.getFoodItems().collect { items ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    featuredItems = items.filter { it.isFeatured }.take(5),
                    popularItems = items.filter { it.isAvailable }.take(6)
                )
            }
        }
    }
}
