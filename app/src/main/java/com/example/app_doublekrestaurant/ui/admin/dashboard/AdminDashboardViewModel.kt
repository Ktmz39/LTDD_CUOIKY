package com.example.app_doublekrestaurant.ui.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.*
import com.example.app_doublekrestaurant.data.repository.OrderRepository
import com.example.app_doublekrestaurant.data.repository.ReservationRepository
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DailyRevenueDetail(
    val date: String,
    val revenue: Double
)

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val totalRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val dailyRevenueDetails: List<DailyRevenueDetail> = emptyList(),
    val monthlyDailyRevenueDetails: List<DailyRevenueDetail> = emptyList(),
    val pendingOrdersCount: Int = 0,
    val pendingReservationsCount: Int = 0,
    val totalFoodItemsCount: Int = 0,
    val totalTableCount: Int = 24, 
    val activeTablesCount: Int = 0,
    val tables: List<RestaurantTable> = emptyList(),
    val weeklyRevenue: List<Double> = emptyList(), 
    val topSellingItems: List<TopSellingItem> = emptyList(),
    val ordersToProcess: List<Order> = emptyList(),
    val error: String? = null
)

data class TopSellingItem(
    val name: String,
    val orderCount: Int,
    val totalRevenue: Double,
    val imageUrl: String
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val reservationRepository: ReservationRepository,
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState(isLoading = true))
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init { loadDashboardData() }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                orderRepository.getOrders(userId = null),      
                reservationRepository.getReservations(userId = null), 
                restaurantRepository.getFoodItems(categoryId = null),
                restaurantRepository.getTables()
            ) { orders, reservations, foodItems, tables ->
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val todayStart = calendar.timeInMillis

                // 1. Total Revenue (Today)
                val todayRevenue = orders
                    .filter { it.status == OrderStatus.COMPLETED.name && it.createdAt >= todayStart }
                    .sumOf { it.totalAmount }

                // 2. Monthly Revenue
                val calMonth = Calendar.getInstance()
                calMonth.set(Calendar.DAY_OF_MONTH, 1)
                calMonth.set(Calendar.HOUR_OF_DAY, 0)
                calMonth.set(Calendar.MINUTE, 0)
                calMonth.set(Calendar.SECOND, 0)
                calMonth.set(Calendar.MILLISECOND, 0)
                val monthStart = calMonth.timeInMillis
                
                val monthlyRev = orders
                    .filter { it.status == OrderStatus.COMPLETED.name && it.createdAt >= monthStart }
                    .sumOf { it.totalAmount }

                // 3. Daily Revenue Details (Last 7 Days)
                val dailyRevDetails = mutableListOf<DailyRevenueDetail>()
                val dateFormat = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
                val weeklyRev = mutableListOf<Double>()
                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    val dateStr = dateFormat.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val end = cal.timeInMillis
                    
                    val dailyRev = orders
                        .filter { it.status == OrderStatus.COMPLETED.name && it.createdAt in start until end }
                        .sumOf { it.totalAmount }
                    
                    weeklyRev.add(dailyRev)
                    dailyRevDetails.add(DailyRevenueDetail(dateStr, dailyRev))
                }

                // 4. Daily Revenue in current month (day 1 to today)
                val monthlyDailyRev = mutableListOf<DailyRevenueDetail>()
                val currentMonthCal = Calendar.getInstance()
                val todayDay = currentMonthCal.get(Calendar.DAY_OF_MONTH)
                val monthDateFormat = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
                
                for (d in 1..todayDay) {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_MONTH, d)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    val dateStr = monthDateFormat.format(cal.time)
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val end = cal.timeInMillis
                    
                    val dailyRev = orders
                        .filter { it.status == OrderStatus.COMPLETED.name && it.createdAt in start until end }
                        .sumOf { it.totalAmount }
                    monthlyDailyRev.add(DailyRevenueDetail(dateStr, dailyRev))
                }
                // Sort descending so the latest days appear first
                monthlyDailyRev.sortByDescending { it.date }

                // 5. Top Selling Items
                val itemMap = mutableMapOf<String, Triple<String, Int, Double>>() // foodItemId -> (Name, Count, Revenue)
                orders.filter { it.status == OrderStatus.COMPLETED.name }.forEach { order ->
                    order.items.forEach { item ->
                        val current = itemMap[item.foodItemId] ?: Triple(item.name, 0, 0.0)
                        itemMap[item.foodItemId] = Triple(
                            item.name,
                            current.second + item.quantity,
                            current.third + (item.price * item.quantity)
                        )
                    }
                }
                
                val topItems = itemMap.values
                    .sortedByDescending { it.second }
                    .take(3)
                    .map { (name, count, rev) ->
                        val foodItem = foodItems.find { it.name == name }
                        TopSellingItem(name, count, rev, foodItem?.imageUrl ?: "")
                    }

                // 6. Occupancy
                val totalTables = if (tables.isNotEmpty()) tables.size else 24
                val activeTables = tables.count { it.status != TableStatus.AVAILABLE.name }

                // 7. Orders to Process
                val processOrders = orders
                    .filter { it.status == OrderStatus.PENDING.name || it.status == OrderStatus.CONFIRMED.name || it.status == OrderStatus.PREPARING.name }
                    .sortedByDescending { it.createdAt }
                    .take(5)

                AdminDashboardUiState(
                    isLoading = false,
                    totalRevenue = todayRevenue,
                    monthlyRevenue = monthlyRev,
                    dailyRevenueDetails = dailyRevDetails,
                    monthlyDailyRevenueDetails = monthlyDailyRev,
                    pendingOrdersCount = orders.count { it.status == OrderStatus.PENDING.name },
                    pendingReservationsCount = reservations.count { it.status == ReservationStatus.PENDING.name },
                    totalFoodItemsCount = foodItems.size,
                    totalTableCount = totalTables,
                    activeTablesCount = activeTables,
                    tables = tables,
                    weeklyRevenue = weeklyRev,
                    topSellingItems = topItems,
                    ordersToProcess = processOrders
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
