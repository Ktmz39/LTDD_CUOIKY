package com.example.app_doublekrestaurant.ui.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Order
import com.example.app_doublekrestaurant.data.model.OrderStatus
import com.example.app_doublekrestaurant.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrderUiState(
    val isLoading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedStatus: String = "ALL",
    val error: String? = null
)

@HiltViewModel
class AdminOrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderUiState())
    val uiState: StateFlow<AdminOrderUiState> = _uiState.asStateFlow()

    init { loadOrders() }

    private fun loadOrders() {
        viewModelScope.launch {
            // Fetch ALL orders (no userId filter = admin view)
            orderRepository.getOrders(userId = null).collect { orders ->
                val filtered = filterByStatus(orders, _uiState.value.selectedStatus)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    orders = orders,
                    filteredOrders = filtered
                )
            }
        }
    }

    fun filterByStatus(statusFilter: String) {
        val filtered = filterByStatus(_uiState.value.orders, statusFilter)
        _uiState.value = _uiState.value.copy(selectedStatus = statusFilter, filteredOrders = filtered)
    }

    private fun filterByStatus(orders: List<Order>, status: String): List<Order> {
        return if (status == "ALL") orders
        else orders.filter { it.status == status }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, newStatus).fold(
                onSuccess = { /* Real-time listener will refresh the list */ },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = "Cập nhật thất bại: ${e.message}")
                }
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
