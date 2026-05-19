package com.example.app_doublekrestaurant.util

import com.example.app_doublekrestaurant.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DataSeeder @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun seedInitialData(): Result<Unit> = try {
        seedCategories()
        seedTables()
        seedFoodItems()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun seedCategories() {
        val categories = listOf(
            Category(name = "Khai vị", order = 1),
            Category(name = "Món chính", order = 2),
            Category(name = "Hải sản", order = 3),
            Category(name = "Lẩu", order = 4),
            Category(name = "Đồ uống", order = 5)
        )
        val collection = firestore.collection("categories")
        val existing = collection.get().await()
        if (existing.isEmpty) {
            categories.forEach { cat ->
                val doc = collection.document()
                collection.document(doc.id).set(cat.copy(id = doc.id)).await()
            }
        }
    }

    private suspend fun seedTables() {
        val tables = mutableListOf<RestaurantTable>()
        for (i in 1..10) {
            tables.add(RestaurantTable(number = i, capacity = if (i <= 4) 2 else 4, floor = 1))
        }
        val collection = firestore.collection("tables")
        val existing = collection.get().await()
        if (existing.isEmpty) {
            tables.forEach { table ->
                val doc = collection.document()
                collection.document(doc.id).set(table.copy(id = doc.id)).await()
            }
        }
    }

    private suspend fun seedFoodItems() {
        val collection = firestore.collection("food_items")
        val existing = collection.get().await()
        if (existing.isEmpty) {
            // We'd need category IDs here to be realistic, but for now we'll just seed a few
            val items = listOf(
                FoodItem(name = "Nem rán truyền thống", price = 65000.0, description = "Nem rán giòn rụm với nhân thịt heo và mộc nhĩ", isFeatured = true),
                FoodItem(name = "Phở bò đặc biệt", price = 85000.0, description = "Nước dùng thanh ngọt từ xương bò hầm 24h", isFeatured = true),
                FoodItem(name = "Bún chả Hà Nội", price = 75000.0, description = "Thịt nướng thơm lừng ăn kèm nước mắm chua ngọt", isFeatured = true),
                FoodItem(name = "Cà phê sữa đá", price = 35000.0, description = "Cà phê hạt Robusta nguyên chất", isFeatured = true)
            )
            items.forEach { item ->
                val doc = collection.document()
                collection.document(doc.id).set(item.copy(id = doc.id)).await()
            }
        }
    }
}
