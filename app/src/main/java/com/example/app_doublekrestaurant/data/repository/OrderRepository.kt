package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Order
import com.example.app_doublekrestaurant.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrders(userId: String? = null): Flow<List<Order>>
    fun getOrderById(orderId: String): Flow<Order?>
    suspend fun placeOrder(order: Order): Result<String>
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit>
}
