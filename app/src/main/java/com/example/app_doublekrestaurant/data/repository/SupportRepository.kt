package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.SupportMessage
import com.example.app_doublekrestaurant.data.model.SupportRoom
import kotlinx.coroutines.flow.Flow

interface SupportRepository {
    fun getChatRooms(): Flow<List<SupportRoom>>
    fun getMessages(roomId: String): Flow<List<SupportMessage>>
    suspend fun sendMessage(userId: String, content: String, isAdmin: Boolean)
    suspend fun markMessagesAsRead(roomId: String, isAdmin: Boolean)
}
