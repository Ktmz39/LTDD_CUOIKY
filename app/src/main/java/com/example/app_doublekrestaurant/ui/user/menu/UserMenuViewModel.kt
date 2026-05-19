package com.example.app_doublekrestaurant.ui.user.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Category
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import com.example.app_doublekrestaurant.ui.user.cart.UserCartViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserMenuUiState(
    val isLoading: Boolean = false,
    val allItems: List<FoodItem> = emptyList(),
    val filteredItems: List<FoodItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class UserMenuViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserMenuUiState(isLoading = true))
    val uiState: StateFlow<UserMenuUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                restaurantRepository.getCategories(),
                restaurantRepository.getFoodItems()
            ) { categories, items ->
                Pair(categories, items)
            }.collect { (categories, items) ->
                val state = _uiState.value
                val filtered = applyFilters(items, state.selectedCategoryId, state.searchQuery)
                _uiState.value = state.copy(
                    isLoading = false,
                    allItems = items,
                    categories = categories,
                    filteredItems = filtered
                )
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        val state = _uiState.value
        val filtered = applyFilters(state.allItems, categoryId, state.searchQuery)
        _uiState.value = state.copy(selectedCategoryId = categoryId, filteredItems = filtered)
    }

    fun search(query: String) {
        val state = _uiState.value
        val filtered = applyFilters(state.allItems, state.selectedCategoryId, query)
        _uiState.value = state.copy(searchQuery = query, filteredItems = filtered)
    }

    private fun applyFilters(items: List<FoodItem>, categoryId: String?, query: String): List<FoodItem> {
        return items.filter { item ->
            val matchCat = categoryId == null || item.categoryId == categoryId
            val matchSearch = query.isEmpty() || item.name.contains(query, ignoreCase = true) || item.description.contains(query, ignoreCase = true)
            matchCat && matchSearch && item.isAvailable
        }
    }
}
