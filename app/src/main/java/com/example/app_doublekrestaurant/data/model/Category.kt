package com.example.app_doublekrestaurant.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = "",
    val order: Int = 0,
    val isActive: Boolean = true
)
