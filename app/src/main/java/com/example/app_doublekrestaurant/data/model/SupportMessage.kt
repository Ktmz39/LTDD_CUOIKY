package com.example.app_doublekrestaurant.data.model

data class SupportMessage(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)
