package com.example.app_doublekrestaurant.ui.admin.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.Reservation
import com.example.app_doublekrestaurant.data.model.ReservationStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingManagementScreen(
    viewModel: AdminBookingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    val tabs = listOf("ALL" to "Tất cả", "PENDING" to "Chờ xác nhận", "CONFIRMED" to "Đã xác nhận", "SEATED" to "Đã vào bàn", "COMPLETED" to "Hoàn thành", "CANCELLED" to "Đã hủy")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Quản lý đặt bàn", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFAC2D00), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOfFirst { it.first == uiState.selectedStatus }.coerceAtLeast(0),
                containerColor = Color.White, contentColor = Color(0xFFAC2D00), edgePadding = 8.dp
            ) {
                tabs.forEach { (status, label) ->
                    Tab(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.filterByStatus(status) },
                        text = {
                            val count = if (status == "ALL") uiState.reservations.size
                            else uiState.reservations.count { it.status == status }
                            Text("$label ($count)", fontSize = 12.sp)
                        }
                    )
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFAC2D00))
                }
                uiState.filteredReservations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TableBar, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text("Không có đặt bàn nào", color = Color.Gray)
                    }
                }
                else -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(uiState.filteredReservations, key = { it.id }) { reservation ->
                        AdminBookingCard(reservation = reservation, onUpdateStatus = { newStatus ->
                            viewModel.updateStatus(reservation.id, newStatus)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookingCard(reservation: Reservation, onUpdateStatus: (ReservationStatus) -> Unit) {
    val currentStatus = reservation.reservationStatus
    val createdStr = remember(reservation.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(reservation.createdAt))
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
                    Text("Bàn ${reservation.tableNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Đặt lúc: $createdStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                BookingStatusBadge(currentStatus)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(Icons.Default.Person, reservation.userName)
                    InfoRow(Icons.Default.Phone, reservation.userPhone)
                    InfoRow(Icons.Default.People, "${reservation.guestCount} khách")
                }
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(Icons.Default.CalendarToday, reservation.date)
                    InfoRow(Icons.Default.Schedule, reservation.time)
                    if (reservation.note.isNotEmpty()) {
                        InfoRow(Icons.Default.Notes, reservation.note)
                    }
                }
            }

            val nextStatus: Pair<ReservationStatus, String>? = when (currentStatus) {
                ReservationStatus.PENDING -> ReservationStatus.CONFIRMED to "Xác nhận"
                ReservationStatus.CONFIRMED -> ReservationStatus.SEATED to "Khách đã vào bàn"
                ReservationStatus.SEATED -> ReservationStatus.COMPLETED to "Hoàn thành"
                else -> null
            }

            if (nextStatus != null || currentStatus == ReservationStatus.PENDING) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentStatus == ReservationStatus.PENDING) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(ReservationStatus.CANCELLED) },
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
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.DarkGray, maxLines = 1)
    }
}

@Composable
fun BookingStatusBadge(status: ReservationStatus) {
    val (label, color) = when (status) {
        ReservationStatus.PENDING -> "Chờ xác nhận" to Color(0xFFFFA000)
        ReservationStatus.CONFIRMED -> "Đã xác nhận" to Color(0xFF1976D2)
        ReservationStatus.SEATED -> "Đã vào bàn" to Color(0xFF7B1FA2)
        ReservationStatus.COMPLETED -> "Hoàn thành" to Color(0xFF388E3C)
        ReservationStatus.CANCELLED -> "Đã hủy" to Color.Gray
        ReservationStatus.NO_SHOW -> "Không đến" to Color(0xFFD32F2F)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = color.copy(0.15f)) {
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}
