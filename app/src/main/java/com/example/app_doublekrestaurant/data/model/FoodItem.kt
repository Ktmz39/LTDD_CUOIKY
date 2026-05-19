package com.example.app_doublekrestaurant.data.model

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val isSpicy: Boolean = false,
    val isVegetarian: Boolean = false,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
