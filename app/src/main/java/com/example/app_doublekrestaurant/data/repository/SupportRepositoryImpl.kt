package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.SupportMessage
import com.example.app_doublekrestaurant.data.model.SupportRoom
import com.example.app_doublekrestaurant.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SupportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : SupportRepository {

    override fun getChatRooms(): Flow<List<SupportRoom>> = callbackFlow {
        val listener = firestore.collection("support_chats")
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val rooms = snapshot?.documents?.mapNotNull { it.toObject(SupportRoom::class.java) }
                    ?.filter { it.lastMessage.isNotEmpty() } ?: emptyList()
                trySend(rooms)
            }
        
        awaitClose { listener.remove() }
    }

    override fun getMessages(roomId: String): Flow<List<SupportMessage>> = callbackFlow {
        val listener = firestore.collection("support_chats").document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { it.toObject(SupportMessage::class.java) } ?: emptyList()
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(userId: String, content: String, isAdmin: Boolean) {
        val senderId = if (isAdmin) "admin" else userId
        val roomId = userId
        val roomRef = firestore.collection("support_chats").document(roomId)
        
        val timestamp = System.currentTimeMillis()
        
        // Ensure room exists before sending
        val roomSnapshot = roomRef.get().await()
        if (!roomSnapshot.exists()) {
            if (!isAdmin) {
                // Fetch user info to create room
                val userSnapshot = firestore.collection("users").document(userId).get().await()
                val user = userSnapshot.toObject(User::class.java)
                val newRoom = SupportRoom(
                    id = roomId,
                    userId = userId,
                    userName = user?.fullName ?: "User",
                    userAvatarUrl = user?.avatarUrl ?: "",
                    lastMessage = content,
                    lastMessageTime = timestamp,
                    unreadCountAdmin = 1,
                    unreadCountUser = 0
                )
                roomRef.set(newRoom).await()
            }
        } else {
            // Update last message and unread count
            val updates = mutableMapOf<String, Any>(
                "lastMessage" to content,
                "lastMessageTime" to timestamp
            )
            if (isAdmin) {
                updates["unreadCountUser"] = FieldValue.increment(1)
            } else {
                updates["unreadCountAdmin"] = FieldValue.increment(1)
            }
            roomRef.update(updates).await()
        }
        
        // Add message
        val messageRef = roomRef.collection("messages").document()
        val message = SupportMessage(
            id = messageRef.id,
            senderId = senderId,
            content = content,
            timestamp = timestamp,
            isRead = false
        )
        messageRef.set(message).await()
    }

    override suspend fun markMessagesAsRead(roomId: String, isAdmin: Boolean) {
        val roomRef = firestore.collection("support_chats").document(roomId)
        if (isAdmin) {
            roomRef.update("unreadCountAdmin", 0).await()
        } else {
            roomRef.update("unreadCountUser", 0).await()
        }
    }
}
