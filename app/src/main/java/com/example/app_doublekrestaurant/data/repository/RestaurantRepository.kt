package com.example.app_doublekrestaurant.data.repository

import kotlin.Result

import com.example.app_doublekrestaurant.data.model.Category
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.data.model.RestaurantTable
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {
    fun getCategories(): Flow<List<Category>>
    fun getFoodItems(categoryId: String? = null): Flow<List<FoodItem>>
    fun getTables(): Flow<List<RestaurantTable>>
    suspend fun updateFoodItem(foodItem: FoodItem): Result<Unit>
    suspend fun deleteFoodItem(foodItemId: String): Result<Unit>
    suspend fun addFoodItem(foodItem: FoodItem): Result<String>
    suspend fun toggleFoodAvailability(foodItemId: String, isAvailable: Boolean): Result<Unit>
    suspend fun updateTable(table: RestaurantTable): Result<Unit>
    suspend fun addTable(table: RestaurantTable): Result<String>
    suspend fun deleteTable(tableId: String): Result<Unit>
    suspend fun updateFoodRating(foodItemId: String, newRating: Double, newReviewCount: Int): Result<Unit>
}
