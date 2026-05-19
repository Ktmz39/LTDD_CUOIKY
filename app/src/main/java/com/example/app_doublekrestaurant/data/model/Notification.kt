package com.example.app_doublekrestaurant.data.model

data class Notification(
    val id: String = "",
    val userId: String = "",       // empty = broadcast to all
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val referenceId: String = "", // orderId / reservationId
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    ORDER_UPDATE,
    RESERVATION_UPDATE,
    PROMOTION,
    GENERAL
}
