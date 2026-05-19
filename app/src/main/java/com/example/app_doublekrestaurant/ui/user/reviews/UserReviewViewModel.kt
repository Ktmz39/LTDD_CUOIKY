package com.example.app_doublekrestaurant.ui.user.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Review
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import com.example.app_doublekrestaurant.data.repository.ReviewRepository
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Result

data class UserReviewUiState(
    val myReviews: List<Review> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UserReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: com.example.app_doublekrestaurant.data.repository.OrderRepository,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserReviewUiState())
    val uiState: StateFlow<UserReviewUiState> = _uiState.asStateFlow()

    private val _selectedOrder = MutableStateFlow<com.example.app_doublekrestaurant.data.model.Order?>(null)
    val selectedOrder: StateFlow<com.example.app_doublekrestaurant.data.model.Order?> = _selectedOrder.asStateFlow()

    // Cache the current user for synchronous access in submitReview
    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                currentUser = user
            }
        }
        loadMyReviews()
    }

    private fun loadMyReviews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.currentUser.flatMapLatest { user ->
                if (user != null) {
                    reviewRepository.getReviews().map { reviews ->
                        reviews.filter { it.userId == user.uid }
                    }
                } else {
                    flowOf(emptyList())
                }
            }.collect { reviews ->
                _uiState.value = _uiState.value.copy(myReviews = reviews, isLoading = false)
            }
        }
    }

    fun loadOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.getOrderById(orderId).collect { order ->
                _selectedOrder.value = order
            }
        }
    }

    fun submitReview(foodItemId: String, foodName: String, rating: Int, comment: String) {
        val user = currentUser ?: run {
            _uiState.value = _uiState.value.copy(error = "Vui lòng đăng nhập để đánh giá")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            val review = Review(
                id = "",
                userId = user.uid,
                userName = user.fullName,
                userAvatarUrl = user.avatarUrl,
                foodItemId = foodItemId,
                foodItemName = foodName,
                rating = rating,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )

            val result = reviewRepository.submitReview(review)
            if (result.isSuccess) {
                // Update food item rating in Firestore
                try {
                    val foodItems = restaurantRepository.getFoodItems().first()
                    val foodItem = foodItems.find { it.id == foodItemId }
                    if (foodItem != null) {
                        val currentRating = foodItem.rating
                        val currentCount = foodItem.reviewCount
                        val newCount = currentCount + 1
                        val newRating = ((currentRating * currentCount) + rating) / newCount
                        restaurantRepository.updateFoodRating(foodItemId, newRating, newCount)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _uiState.value = _uiState.value.copy(isSubmitting = false, successMessage = "Cảm ơn bạn đã đánh giá!")
            } else {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = "Gửi đánh giá thất bại")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
