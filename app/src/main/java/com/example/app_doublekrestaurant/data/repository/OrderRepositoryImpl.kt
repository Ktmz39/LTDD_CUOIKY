package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Order
import com.example.app_doublekrestaurant.data.model.OrderStatus
import com.example.app_doublekrestaurant.data.model.NotificationType
import com.example.app_doublekrestaurant.util.formatVnd
import com.example.app_doublekrestaurant.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Result

class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) : OrderRepository {

    override fun getOrders(userId: String?): Flow<List<Order>> = callbackFlow {
        val query: Query = if (userId != null) {
            firestore.collection("orders")
                .whereEqualTo("userId", userId)
        } else {
            firestore.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) { 
                android.util.Log.e("OrderRepository", "Listen failed.", error)
                trySend(emptyList())
                return@addSnapshotListener 
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    override fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val subscription = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, _ ->
                val order = snapshot?.toObject(Order::class.java)?.copy(id = snapshot.id)
                trySend(order)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun placeOrder(order: Order): Result<String> = try {
        val docRef = firestore.collection("orders").document()
        val finalOrder = order.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        docRef.set(finalOrder).await()
        
        // Send notification to Admin
        notificationRepository.sendNotification(
            Notification(
                title = "Đơn hàng mới! 📦",
                body = "Khách hàng ${order.userName} vừa đặt đơn hàng mới trị giá ${formatVnd(order.totalAmount)}",
                userId = "", // Broadcast to admin
                type = NotificationType.ORDER_UPDATE,
                referenceId = docRef.id
            )
        )

        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> = try {
        firestore.collection("orders").document(orderId)
            .update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()

        // Get order details to send notification to User
        val order = getOrderById(orderId).firstOrNull()
        if (order != null) {
            val statusMsg = when (status) {
                OrderStatus.CONFIRMED -> "Đơn hàng của bạn đã được xác nhận"
                OrderStatus.PREPARING -> "Nhà hàng đang chuẩn bị món ăn của bạn"
                OrderStatus.READY -> "Món ăn đã sẵn sàng! 🍽️"
                OrderStatus.DELIVERING -> "Đơn hàng đang trên đường đến với bạn 🛵"
                OrderStatus.COMPLETED -> "Đơn hàng đã hoàn thành. Chúc bạn ngon miệng!"
                OrderStatus.CANCELLED -> "Rất tiếc, đơn hàng của bạn đã bị hủy"
                else -> ""
            }
            if (statusMsg.isNotEmpty()) {
                notificationRepository.sendNotification(
                    Notification(
                        title = "Cập nhật đơn hàng",
                        body = statusMsg,
                        userId = order.userId,
                        type = NotificationType.ORDER_UPDATE,
                        referenceId = orderId
                    )
                )
            }
        }

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun cancelOrder(orderId: String): Result<Unit> = try {
        firestore.collection("orders").document(orderId)
            .update(
                mapOf(
                    "status" to OrderStatus.CANCELLED.name,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
