package com.example.app_doublekrestaurant.data.model

data class Reservation(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val date: String = "",           // "yyyy-MM-dd"
    val time: String = "",           // "HH:mm"
    val guestCount: Int = 1,
    val tableId: String = "",
    val tableNumber: Int = 0,
    val note: String = "",
    val status: String = ReservationStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Safe enum accessor
    val reservationStatus: ReservationStatus
        get() = try { ReservationStatus.valueOf(status) } catch (e: Exception) { ReservationStatus.PENDING }
}

enum class ReservationStatus {
    PENDING,    // Chờ xác nhận
    CONFIRMED,  // Đã xác nhận
    SEATED,     // Đã vào bàn
    COMPLETED,  // Hoàn thành
    CANCELLED,  // Đã hủy
    NO_SHOW     // Không đến
}
