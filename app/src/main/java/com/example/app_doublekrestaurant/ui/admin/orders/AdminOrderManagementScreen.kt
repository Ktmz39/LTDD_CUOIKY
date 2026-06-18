package com.example.app_doublekrestaurant.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.Order
import com.example.app_doublekrestaurant.data.model.OrderStatus
import com.example.app_doublekrestaurant.util.formatVnd
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderManagementScreen(
    viewModel: AdminOrderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    val statusTabs = listOf("ALL" to "Tất cả", "PENDING" to "Chờ duyệt", "CONFIRMED" to "Đã xác nhận", "PREPARING" to "Đang làm", "COMPLETED" to "Hoàn thành", "CANCELLED" to "Đã hủy")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đơn hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFAC2D00), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = statusTabs.indexOfFirst { it.first == uiState.selectedStatus }.coerceAtLeast(0),
                containerColor = Color.White, contentColor = Color(0xFFAC2D00), edgePadding = 8.dp
            ) {
                statusTabs.forEach { (status, label) ->
                    Tab(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.filterByStatus(status) },
                        text = {
                            val count = if (status == "ALL") uiState.orders.size
                            else uiState.orders.count { it.status == status }
                            Text("$label ($count)", fontSize = 12.sp)
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFAC2D00))
                }
                uiState.filteredOrders.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text("Không có đơn hàng nào", color = Color.Gray)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.filteredOrders, key = { it.id }) { order ->
                        AdminOrderCard(order = order, onUpdateStatus = { newStatus ->
                            viewModel.updateOrderStatus(order.id, newStatus)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Order, onUpdateStatus: (OrderStatus) -> Unit) {
    val currentStatus = order.orderStatus
    val dateStr = remember(order.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("#${order.id.take(8).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (order.paymentMethod == "PAY_NOW") {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE3F2FD)) {
                            Text("Cổng QR", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    } else {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8F5E9)) {
                            Text("Tiền mặt", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                    OrderStatusBadge(currentStatus)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFAC2D00).copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (order.userAvatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = order.userAvatarUrl,
                            contentDescription = order.userName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(order.userName.firstOrNull()?.toString() ?: "U", color = Color(0xFFAC2D00), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(order.userName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (order.userPhone.isNotEmpty()) Text(order.userPhone, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(6.dp))
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(16.dp), tint = Color.LightGray)
                        }
                    }
                    Text("${item.name} x${item.quantity}", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.DarkGray)
                    Text(formatVnd(item.subTotal), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng: ${formatVnd(order.totalAmount)}", fontWeight = FontWeight.Bold, color = Color(0xFFAC2D00), fontSize = 15.sp)
                if (order.note.isNotEmpty()) {
                    Text("📝 ${order.note}", fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                }
            }

            // ĐÃ SỬA: Chuẩn hóa logic gán nhãn nút bấm để khử sạch lỗi cảnh báo Always False
            val nextStatus = when (currentStatus) {
                OrderStatus.PENDING -> {
                    val label = if (order.paymentMethod == "PAY_NOW") "Đã nhận tiền & Duyệt" else "Xác nhận"
                    OrderStatus.CONFIRMED to label
                }
                OrderStatus.CONFIRMED -> OrderStatus.PREPARING to "Bắt đầu làm"
                OrderStatus.PREPARING -> OrderStatus.READY to "Sẵn sàng"
                OrderStatus.READY -> OrderStatus.COMPLETED to "Hoàn thành"
                else -> null
            }

            if (nextStatus != null || currentStatus == OrderStatus.PENDING) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentStatus == OrderStatus.PENDING) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(OrderStatus.CANCELLED) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) { Text("Hủy", fontSize = 12.sp) }
                    }
                    nextStatus?.let { (status, label) ->
                        Button(
                            onClick = { onUpdateStatus(status) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00))
                        ) { Text(label, fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val (label, color) = when (status) {
        OrderStatus.PENDING -> "Chờ xác nhận" to Color(0xFFFFA000)
        OrderStatus.CONFIRMED -> "Đã xác nhận" to Color(0xFF1976D2)
        OrderStatus.PREPARING -> "Đang làm" to Color(0xFF7B1FA2)
        OrderStatus.READY -> "Sẵn sàng" to Color(0xFF2E7D32)
        OrderStatus.DELIVERING -> "Đang giao" to Color(0xFF00796B)
        OrderStatus.COMPLETED -> "Hoàn thành" to Color(0xFF388E3C)
        OrderStatus.CANCELLED -> "Đã hủy" to Color.Gray
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(0.15f)) {
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}