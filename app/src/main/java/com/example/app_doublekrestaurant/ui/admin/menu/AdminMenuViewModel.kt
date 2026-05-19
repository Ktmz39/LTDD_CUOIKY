package com.example.app_doublekrestaurant.ui.admin.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminMenuUiState(
    val isLoading: Boolean = true,
    val items: List<FoodItem> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminMenuViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminMenuUiState())
    val uiState: StateFlow<AdminMenuUiState> = _uiState.asStateFlow()

    init { loadItems() }

    private fun loadItems() {
        viewModelScope.launch {
            restaurantRepository.getFoodItems().collect { items ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = items.sortedBy { it.name }
                )
            }
        }
    }

    fun toggleAvailability(item: FoodItem) {
        viewModelScope.launch {
            restaurantRepository.toggleFoodAvailability(item.id, !item.isAvailable).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        successMessage = if (!item.isAvailable) "${item.name} đã bật lại" else "${item.name} đã tạm ngừng"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = "Cập nhật thất bại: ${e.message}")
                }
            )
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            restaurantRepository.deleteFoodItem(itemId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Đã xóa món thành công") },
                onFailure = { e -> _uiState.value = _uiState.value.copy(error = "Xóa thất bại: ${e.message}") }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
