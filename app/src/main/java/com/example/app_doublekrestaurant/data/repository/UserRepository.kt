package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlin.Result

interface UserRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun updateUserRole(userId: String, role: String): Result<Unit>
    suspend fun toggleUserActive(userId: String, isActive: Boolean): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun claimVoucher(userId: String, voucherId: String): Result<Unit>
}
