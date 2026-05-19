package com.example.app_doublekrestaurant.ui.admin.menu

import kotlin.Result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Category
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.app_doublekrestaurant.util.CloudinaryService

data class AddEditFoodUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    // Form fields
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val categoryId: String = "",
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val isSpicy: Boolean = false,
    val isVegetarian: Boolean = false,
    val imageUrl: String = "",
    val isUploadingImage: Boolean = false,
    val uploadProgress: Float = 0f
)

@HiltViewModel
class AdminAddEditFoodViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditFoodUiState())
    val uiState: StateFlow<AddEditFoodUiState> = _uiState.asStateFlow()

    private var foodItemId: String? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            restaurantRepository.getCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
                if (categories.isNotEmpty() && _uiState.value.categoryId.isEmpty()) {
                    _uiState.value = _uiState.value.copy(categoryId = categories.first().id)
                }
            }
        }
    }

    fun loadFoodItem(id: String) {
        foodItemId = id
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            restaurantRepository.getFoodItems().collect { items ->
                val item = items.find { it.id == id }
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = item.name,
                        price = item.price.toInt().toString(),
                        description = item.description,
                        categoryId = item.categoryId,
                        isAvailable = item.isAvailable,
                        isFeatured = item.isFeatured,
                        isSpicy = item.isSpicy,
                        isVegetarian = item.isVegetarian,
                        imageUrl = item.imageUrl
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun onPriceChange(value: String) { _uiState.value = _uiState.value.copy(price = value) }
    fun onDescriptionChange(value: String) { _uiState.value = _uiState.value.copy(description = value) }
    fun onCategoryChange(value: String) { _uiState.value = _uiState.value.copy(categoryId = value) }
    fun onAvailableChange(value: Boolean) { _uiState.value = _uiState.value.copy(isAvailable = value) }
    fun onFeaturedChange(value: Boolean) { _uiState.value = _uiState.value.copy(isFeatured = value) }
    fun onSpicyChange(value: Boolean) { _uiState.value = _uiState.value.copy(isSpicy = value) }
    fun onVegetarianChange(value: Boolean) { _uiState.value = _uiState.value.copy(isVegetarian = value) }
    fun onImageUrlChange(value: String) { _uiState.value = _uiState.value.copy(imageUrl = value) }

    fun uploadImage(uri: android.net.Uri) {
        _uiState.value = _uiState.value.copy(isUploadingImage = true, uploadProgress = 0f)
        CloudinaryService.uploadImage(
            uri = uri,
            onSuccess = { url ->
                _uiState.value = _uiState.value.copy(imageUrl = url, isUploadingImage = false)
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(error = error, isUploadingImage = false)
            },
            onProgress = { progress ->
                _uiState.value = _uiState.value.copy(uploadProgress = progress.toFloat())
            }
        )
    }

    fun saveFoodItem() {
        val state = _uiState.value
        if (state.name.isBlank() || state.price.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng nhập tên và giá món ăn")
            return
        }

        val priceValue = state.price.toDoubleOrNull() ?: 0.0
        val categoryName = state.categories.find { it.id == state.categoryId }?.name ?: ""

        val foodItem = FoodItem(
            id = foodItemId ?: "",
            name = state.name,
            price = priceValue,
            description = state.description,
            categoryId = state.categoryId,
            categoryName = categoryName,
            isAvailable = state.isAvailable,
            isFeatured = state.isFeatured,
            isSpicy = state.isSpicy,
            isVegetarian = state.isVegetarian,
            imageUrl = state.imageUrl
        )

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = if (foodItemId == null) {
                restaurantRepository.addFoodItem(foodItem)
            } else {
                restaurantRepository.updateFoodItem(foodItem)
            }

            result.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true) },
                onFailure = { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
