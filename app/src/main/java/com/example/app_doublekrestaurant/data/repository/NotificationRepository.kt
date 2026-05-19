package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlin.Result

interface NotificationRepository {
    fun getNotifications(userId: String? = null): Flow<List<Notification>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun sendNotification(notification: Notification): Result<String>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
}
