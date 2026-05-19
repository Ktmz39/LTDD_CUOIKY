package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.model.UserRole
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(user: User, password: String): Result<User>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
    suspend fun updateAvatarUrl(uid: String, url: String): Result<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit>
}
