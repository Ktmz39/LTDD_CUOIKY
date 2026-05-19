package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviews(foodItemId: String? = null): Flow<List<Review>>
    suspend fun submitReview(review: Review): Result<Unit>
    suspend fun deleteReview(reviewId: String): Result<Unit>
}
