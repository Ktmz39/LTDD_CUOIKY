package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Result

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getNotifications(userId: String?): Flow<List<Notification>> = callbackFlow {
        var query: Query = firestore.collection("notifications")
        
        if (userId != null) {
            // Get notifications for specific user OR global ones (userId == "")
            query = query.whereIn("userId", listOf(userId, ""))
        } else {
            // For admin, maybe get all or only admin-specific ones
            // Assuming admin wants to see all notifications sent to system
        }
        
        val subscription = query.addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Notification::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(list.sortedByDescending { it.createdAt })
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications").document(notificationId).update("isRead", true).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun sendNotification(notification: Notification): Result<String> = try {
        val docRef = firestore.collection("notifications").document()
        val finalNotif = notification.copy(id = docRef.id, createdAt = System.currentTimeMillis())
        docRef.set(finalNotif).await()
        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> = try {
        firestore.collection("notifications").document(notificationId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
