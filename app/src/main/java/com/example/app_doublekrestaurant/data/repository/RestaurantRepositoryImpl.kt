package com.example.app_doublekrestaurant.data.repository

import kotlin.Result

import com.example.app_doublekrestaurant.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RestaurantRepository {

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
        val subscription = firestore.collection("categories")
            .orderBy("order")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override fun getFoodItems(categoryId: String?): Flow<List<FoodItem>> = callbackFlow {
        var query: Query = firestore.collection("food_items")
        if (categoryId != null) {
            query = query.whereEqualTo("categoryId", categoryId)
        }
        
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(FoodItem::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    override fun getTables(): Flow<List<RestaurantTable>> = callbackFlow {
        val subscription = firestore.collection("tables")
            .orderBy("number")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RestaurantTable::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateFoodItem(foodItem: FoodItem): Result<Unit> = try {
        firestore.collection("food_items").document(foodItem.id).set(foodItem).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteFoodItem(foodItemId: String): Result<Unit> = try {
        firestore.collection("food_items").document(foodItemId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun addFoodItem(foodItem: FoodItem): Result<String> = try {
        val docRef = firestore.collection("food_items").document()
        val item = foodItem.copy(id = docRef.id)
        docRef.set(item).await()
        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun toggleFoodAvailability(foodItemId: String, isAvailable: Boolean): Result<Unit> = try {
        firestore.collection("food_items").document(foodItemId)
            .update("isAvailable", isAvailable).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateTable(table: RestaurantTable): Result<Unit> = try {
        firestore.collection("tables").document(table.id).set(table).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun addTable(table: RestaurantTable): Result<String> = try {
        val docRef = firestore.collection("tables").document()
        val finalTable = table.copy(id = docRef.id)
        docRef.set(finalTable).await()
        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteTable(tableId: String): Result<Unit> = try {
        firestore.collection("tables").document(tableId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateFoodRating(foodItemId: String, newRating: Double, newReviewCount: Int): Result<Unit> = try {
        firestore.collection("food_items").document(foodItemId).update(
            mapOf(
                "rating" to newRating,
                "reviewCount" to newReviewCount
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
