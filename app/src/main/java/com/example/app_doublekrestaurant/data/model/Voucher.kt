package com.example.app_doublekrestaurant.data.model

data class Voucher(
    val id: String = "",
    val title: String = "",
    val type: String = "PERCENTAGE", // "PERCENTAGE", "AMOUNT", "GIFT"
    val iconType: String = "TICKET", // "TICKET", "CONFETTI", "FIRE", "GIFT"
    val startDate: Long = System.currentTimeMillis(),
    val code: String = "",
    val description: String = "",
    val discountAmount: Double = 0.0,
    val discountPercentage: Int = 0, // 0-100
    val minOrderAmount: Double = 0.0,
    val maxDiscountAmount: Double = 0.0,
    val expiryDate: Long = 0L,
    val isActive: Boolean = true,
    val usageLimit: Int = 0,
    val usedCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
