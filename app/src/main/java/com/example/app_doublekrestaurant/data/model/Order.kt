package com.example.app_doublekrestaurant.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userAvatarUrl: String = "",
    val items: List<OrderItem> = emptyList(),
    val type: String = OrderType.DINE_IN.name,
    val status: String = OrderStatus.PENDING.name,
    val tableId: String = "",
    val tableNumber: Int = 0,
    val deliveryAddress: String = "",
    val totalAmount: Double = 0.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val staffId: String = ""
) {
    // Helper accessors to convert String -> Enum safely
    val orderStatus: OrderStatus get() = try { OrderStatus.valueOf(status) } catch (e: Exception) { OrderStatus.PENDING }
    val orderType: OrderType get() = try { OrderType.valueOf(type) } catch (e: Exception) { OrderType.DINE_IN }
}

data class OrderItem(
    val foodItemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val note: String = "",
    val imageUrl: String = ""
) {
    val subTotal: Double get() = price * quantity
}

enum class OrderType {
    DINE_IN,       // Ăn tại quán
    TAKEAWAY,      // Mang về
    RESERVATION    // Đặt bàn
}

enum class OrderStatus {
    PENDING,       // Chờ xác nhận
    CONFIRMED,     // Đã xác nhận
    PREPARING,     // Đang chuẩn bị
    READY,         // Sẵn sàng
    DELIVERING,    // Đang giao
    COMPLETED,     // Hoàn thành
    CANCELLED      // Đã hủy
}
