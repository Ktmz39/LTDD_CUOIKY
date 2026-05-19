package com.example.app_doublekrestaurant.ui.user.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EventNote
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
fun UserReservationHistoryScreen(
    viewModel: ReservationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch đặt bàn của tôi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
        } else if (uiState.reservations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventNote, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Bạn chưa có lịch đặt bàn nào", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.reservations) { reservation ->
                    UserReservationCard(reservation)
                }
            }
        }
    }
}

@Composable
fun UserReservationCard(reservation: Reservation) {
    val statusColor = when (reservation.status) {
        ReservationStatus.CONFIRMED.name -> Color(0xFF388E3C)
        ReservationStatus.CANCELLED.name -> Color.Red
        else -> Color(0xFFFFA000)
    }
    
    val statusText = when (reservation.status) {
        ReservationStatus.PENDING.name -> "Chờ xác nhận"
        ReservationStatus.CONFIRMED.name -> "Đã xác nhận"
        ReservationStatus.CANCELLED.name -> "Đã hủy"
        ReservationStatus.SEATED.name -> "Đã nhận bàn"
        ReservationStatus.COMPLETED.name -> "Hoàn thành"
        else -> reservation.status
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
                    Text("Bàn số ${reservation.tableNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${reservation.date} | ${reservation.time}", fontSize = 13.sp, color = Color.Gray)
                }
                Surface(
                    color = statusColor.copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Số khách: ${reservation.guestCount}", fontSize = 14.sp)
            if (reservation.note.isNotEmpty()) {
                Text("Ghi chú: ${reservation.note}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
