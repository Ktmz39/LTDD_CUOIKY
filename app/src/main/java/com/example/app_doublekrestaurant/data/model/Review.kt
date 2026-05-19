package com.example.app_doublekrestaurant.data.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val orderId: String = "",
    val foodItemId: String = "",
    val foodItemName: String = "",       // Added for display
    val rating: Int = 5,                // 1-5 stars
    val comment: String = "",
    val imageUrls: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val adminReply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
