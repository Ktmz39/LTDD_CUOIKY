package com.example.app_doublekrestaurant.data.model

data class SupportRoom(
    val id: String = "", // Same as userId
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCountAdmin: Int = 0,
    val unreadCountUser: Int = 0,
    val isActive: Boolean = true
)
