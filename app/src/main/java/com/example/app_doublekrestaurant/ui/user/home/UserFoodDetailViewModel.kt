package com.example.app_doublekrestaurant.ui.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.data.model.Review
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import com.example.app_doublekrestaurant.data.repository.ReviewRepository
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodDetailUiState(
    val foodItem: FoodItem? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UserFoodDetailViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()
    
    private var currentUser: User? = null
    private var detailJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUser = user
            }
        }
    }

    fun loadFoodDetail(foodId: String) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Combine food item and its reviews
            restaurantRepository.getFoodItems().flatMapLatest { items ->
                val food = items.find { it.id == foodId }
                if (food != null) {
                    reviewRepository.getReviews(foodId).map { reviews ->
                        food to reviews
                    }
                } else {
                    flowOf(null to emptyList<Review>())
                }
            }.collect { (food, reviews) ->
                if (food != null) {
                    _uiState.value = _uiState.value.copy(
                        foodItem = food,
                        reviews = reviews.sortedByDescending { it.createdAt },
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Không tìm thấy món ăn")
                }
            }
        }
    }

    fun submitReview(rating: Int, comment: String) {
        val food = _uiState.value.foodItem ?: return
        val user = currentUser ?: run {
            _uiState.value = _uiState.value.copy(error = "Vui lòng đăng nhập để đánh giá")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingReview = true, error = null)
            val review = Review(
                id = "",
                userId = user.uid,
                userName = user.fullName,
                userAvatarUrl = user.avatarUrl,
                foodItemId = food.id,
                foodItemName = food.name,
                rating = rating,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )

            val result = reviewRepository.submitReview(review)
            if (result.isSuccess) {
                // Update Food Item Rating
                val currentRating = food.rating
                val currentCount = food.reviewCount
                val newCount = currentCount + 1
                val newRating = ((currentRating * currentCount) + rating) / newCount
                
                restaurantRepository.updateFoodRating(food.id, newRating, newCount)

                _uiState.value = _uiState.value.copy(
                    isSubmittingReview = false, 
                    successMessage = "Cảm ơn bạn đã đánh giá!"
                )
            } else {
                _uiState.value = _uiState.value.copy(isSubmittingReview = false, error = "Gửi đánh giá thất bại")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
