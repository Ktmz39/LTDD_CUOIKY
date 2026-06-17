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
                try {
                    // Parse thủ công để tránh crash khi createdAt là Firestore Timestamp hoặc Long ms
                    val createdAt = safeGetLongMs(doc, "createdAt")
                    val updatedAt = safeGetLongMs(doc, "updatedAt")

                    @Suppress("UNCHECKED_CAST")
                    val rawItems = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
                    val items = rawItems.map { m ->
                        com.example.app_doublekrestaurant.data.model.OrderItem(
                            foodItemId = m["foodItemId"] as? String ?: "",
                            name = m["name"] as? String ?: "",
                            price = (m["price"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (m["quantity"] as? Number)?.toInt() ?: 1,
                            note = m["note"] as? String ?: "",
                            imageUrl = m["imageUrl"] as? String ?: ""
                        )
                    }
                    Order(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        userPhone = doc.getString("userPhone") ?: "",
                        userAvatarUrl = doc.getString("userAvatarUrl") ?: "",
                        items = items,
                        type = doc.getString("type") ?: "TAKEAWAY",
                        status = doc.getString("status") ?: "PENDING",
                        tableId = doc.getString("tableId") ?: "",
                        tableNumber = (doc.getLong("tableNumber") ?: 0L).toInt(),
                        deliveryAddress = doc.getString("deliveryAddress") ?: "",
                        totalAmount = (doc.getDouble("totalAmount") ?: doc.getLong("totalAmount")?.toDouble()) ?: 0.0,
                        note = doc.getString("note") ?: "",
                        paymentMethod = doc.getString("paymentMethod") ?: "PAY_ON_DELIVERY",
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        staffId = doc.getString("staffId") ?: ""
                    )
                } catch (e: Exception) {
                    android.util.Log.e("OrderRepo", "Parse order ${doc.id} failed: ${e.message}")
                    null
                }
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    // Helper: đọc field có thể là Long (ms) hoặc Firestore Timestamp
    private fun safeGetLongMs(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        field: String
    ): Long = try {
        doc.getLong(field)
            ?: doc.getTimestamp(field)?.let { it.seconds * 1000 + it.nanoseconds / 1_000_000 }
            ?: 0L
    } catch (e: Exception) {
        0L
    }


    override fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val subscription = firestore.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists()) { trySend(null); return@addSnapshotListener }
                try {
                    @Suppress("UNCHECKED_CAST")
                    val rawItems = snapshot.get("items") as? List<Map<String, Any?>> ?: emptyList()
                    val items = rawItems.map { m ->
                        com.example.app_doublekrestaurant.data.model.OrderItem(
                            foodItemId = m["foodItemId"] as? String ?: "",
                            name = m["name"] as? String ?: "",
                            price = (m["price"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (m["quantity"] as? Number)?.toInt() ?: 1,
                            note = m["note"] as? String ?: "",
                            imageUrl = m["imageUrl"] as? String ?: ""
                        )
                    }
                    val order = Order(
                        id = snapshot.id,
                        userId = snapshot.getString("userId") ?: "",
                        userName = snapshot.getString("userName") ?: "",
                        userPhone = snapshot.getString("userPhone") ?: "",
                        userAvatarUrl = snapshot.getString("userAvatarUrl") ?: "",
                        items = items,
                        type = snapshot.getString("type") ?: "TAKEAWAY",
                        status = snapshot.getString("status") ?: "PENDING",
                        tableId = snapshot.getString("tableId") ?: "",
                        tableNumber = (snapshot.getLong("tableNumber") ?: 0L).toInt(),
                        deliveryAddress = snapshot.getString("deliveryAddress") ?: "",
                        totalAmount = (snapshot.getDouble("totalAmount") ?: snapshot.getLong("totalAmount")?.toDouble()) ?: 0.0,
                        note = snapshot.getString("note") ?: "",
                        paymentMethod = snapshot.getString("paymentMethod") ?: "PAY_ON_DELIVERY",
                        createdAt = safeGetLongMs(snapshot, "createdAt"),
                        updatedAt = safeGetLongMs(snapshot, "updatedAt"),
                        staffId = snapshot.getString("staffId") ?: ""
                    )
                    trySend(order)
                } catch (e: Exception) {
                    android.util.Log.e("OrderRepo", "Parse getOrderById failed: ${e.message}")
                    trySend(null)
                }

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
