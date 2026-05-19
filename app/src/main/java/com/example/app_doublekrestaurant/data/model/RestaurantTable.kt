package com.example.app_doublekrestaurant.data.model

data class RestaurantTable(
    val id: String = "",
    val number: Int = 0,
    val capacity: Int = 4,
    val floor: Int = 1,
    val status: String = TableStatus.AVAILABLE.name,
    val currentOrderId: String = "",
    val note: String = ""
) {
    // Safe enum accessor
    val tableStatus: TableStatus
        get() = try { TableStatus.valueOf(status) } catch (e: Exception) { TableStatus.AVAILABLE }

    val isAvailable: Boolean
        get() = tableStatus == TableStatus.AVAILABLE
}

enum class TableStatus {
    AVAILABLE,   // Trống
    OCCUPIED,    // Đang sử dụng
    RESERVED,    // Đã đặt trước
    UNAVAILABLE  // Không khả dụng
}
