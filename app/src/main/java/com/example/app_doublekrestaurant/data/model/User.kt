package com.example.app_doublekrestaurant.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val role: UserRole = UserRole.USER,  // Stored as String in Firestore, deserialized via @PropertyName or safe accessor
    val address: String = "",
    val createdAt: Long = 0L,
    val isActive: Boolean = true,
    val claimedVouchers: List<String> = emptyList()
)

enum class UserRole {
    ADMIN, STAFF, USER
}
