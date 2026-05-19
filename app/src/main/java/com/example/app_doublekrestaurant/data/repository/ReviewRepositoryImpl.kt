package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override fun getReviews(foodItemId: String?): Flow<List<Review>> = callbackFlow {
        val query: Query = if (foodItemId != null) {
            firestore.collection("reviews")
                .whereEqualTo("foodItemId", foodItemId)
        } else {
            firestore.collection("reviews")
        }
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun submitReview(review: Review): Result<Unit> = try {
        val docRef = firestore.collection("reviews").document()
        val finalReview = review.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(finalReview).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteReview(reviewId: String): Result<Unit> = try {
        firestore.collection("reviews").document(reviewId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
