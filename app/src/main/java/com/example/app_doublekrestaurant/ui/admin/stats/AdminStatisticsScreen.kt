package com.example.app_doublekrestaurant.ui.admin.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.OrderStatus
import com.example.app_doublekrestaurant.data.repository.OrderRepository
import com.example.app_doublekrestaurant.data.repository.ReservationRepository
import com.example.app_doublekrestaurant.util.formatVnd
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatUiState(
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val pendingOrders: Int = 0,
    val totalReservations: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class AdminStatisticsViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatUiState())
    val uiState: StateFlow<StatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                orderRepository.getOrders(null),
                reservationRepository.getReservations(null)
            ) { orders, reservations ->
                StatUiState(
                    isLoading = false,
                    totalRevenue = orders.filter { it.status == OrderStatus.COMPLETED.name }.sumOf { it.totalAmount },
                    totalOrders = orders.size,
                    completedOrders = orders.count { it.status == OrderStatus.COMPLETED.name },
                    cancelledOrders = orders.count { it.status == OrderStatus.CANCELLED.name },
                    pendingOrders = orders.count { it.status == OrderStatus.PENDING.name },
                    totalReservations = reservations.size
                )
            }.collect { _uiState.value = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(
    viewModel: AdminStatisticsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFAC2D00), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Revenue Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFAC2D00))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Tổng doanh thu", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Text(formatVnd(state.totalRevenue), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Từ ${state.completedOrders} đơn hoàn thành", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                }

                Text("Chi tiết đơn hàng", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatDetailCard("Tổng đơn", "${state.totalOrders}", Icons.Default.Receipt, Color(0xFF1976D2), Modifier.weight(1f))
                    StatDetailCard("Hoàn thành", "${state.completedOrders}", Icons.Default.CheckCircle, Color(0xFF388E3C), Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatDetailCard("Chờ xử lý", "${state.pendingOrders}", Icons.Default.PendingActions, Color(0xFFFFA000), Modifier.weight(1f))
                    StatDetailCard("Đã hủy", "${state.cancelledOrders}", Icons.Default.Cancel, Color(0xFFD32F2F), Modifier.weight(1f))
                }

                Text("Đặt bàn", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    StatDetailCard("Tổng đặt bàn", "${state.totalReservations}", Icons.Default.TableBar, Color(0xFF7B1FA2), Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatDetailCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
                Text(label, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
