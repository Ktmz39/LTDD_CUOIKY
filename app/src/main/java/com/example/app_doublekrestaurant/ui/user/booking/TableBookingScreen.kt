package com.example.app_doublekrestaurant.ui.user.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.RestaurantTable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableBookingScreen(
    viewModel: BookingViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val amenities = listOf("Phòng VIP", "Sinh nhật", "Hội nghị", "Lãng mạn", "Gia đình", "Có trẻ em")
    val timeSlots = listOf("10:00", "11:00", "12:00", "13:00", "14:00", "17:00", "18:00", "19:00", "20:00", "21:00")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Đặt bàn", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFAC2D00), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.submitBooking(onSuccess) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00)),
                    enabled = !uiState.isSubmitting &&
                            uiState.selectedTableId != null &&
                            uiState.date.isNotEmpty() &&
                            uiState.time.isNotEmpty()
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.EventAvailable, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Xác nhận đặt bàn", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Date & Time Section
            BookingSection(title = "Ngày & Giờ") {
                // Date Input
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { viewModel.updateDate(it) },
                    label = { Text("Ngày đặt bàn (yyyy-MM-dd) *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color(0xFFAC2D00)) },
                    placeholder = { Text("2024-12-31") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFAC2D00), focusedLabelColor = Color(0xFFAC2D00))
                )
                Spacer(Modifier.height(8.dp))
                Text("Giờ đặt bàn *", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                // Time Grid
                val chunked = timeSlots.chunked(4)
                chunked.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { time ->
                            val isSelected = uiState.time == time
                            Surface(
                                modifier = Modifier.weight(1f).clickable { viewModel.updateTime(time) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFAC2D00) else Color(0xFFF5F5F5)
                            ) {
                                Text(
                                    time,
                                    color = if (isSelected) Color.White else Color.DarkGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        // Fill remaining cells
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Guest Count
            BookingSection(title = "Số khách") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Số lượng khách", fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.updateGuestCount(uiState.guestCount - 1) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, null, tint = Color(0xFFAC2D00))
                        }
                        Text("${uiState.guestCount}", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 12.dp))
                        IconButton(onClick = { viewModel.updateGuestCount(uiState.guestCount + 1) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFFAC2D00))
                        }
                    }
                }
            }

            // Table Selection
            BookingSection(title = "Chọn bàn ${if (uiState.isLoading) "(Đang tải...)" else "(${uiState.tables.size} bàn)"}") {
                if (uiState.isLoading) {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFAC2D00), modifier = Modifier.size(32.dp))
                    }
                } else if (uiState.tables.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có thông tin bàn. Vui lòng liên hệ nhà hàng.", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    val tableRows = uiState.tables.chunked(3)
                    tableRows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { table ->
                                val isAvailable = viewModel.isTableAvailableForBooking(table, uiState.date, uiState.time)
                                TableChip(
                                    table = table,
                                    isAvailable = isAvailable,
                                    isSelected = uiState.selectedTableId == table.id,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isAvailable) viewModel.selectTable(table.id)
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            // Amenities
            BookingSection(title = "Tiện ích thêm (tùy chọn)") {
                val amenityRows = amenities.chunked(3)
                amenityRows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { amenity ->
                            val isSelected = uiState.selectedAmenities.contains(amenity)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleAmenity(amenity) },
                                label = { Text(amenity, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFAC2D00),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Note
            BookingSection(title = "Ghi chú") {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { viewModel.updateNote(it) },
                    placeholder = { Text("Yêu cầu đặc biệt, dị ứng thực phẩm...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp), maxLines = 4,
                    colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFAC2D00))
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun BookingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFAC2D00))
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun TableChip(
    table: RestaurantTable,
    isAvailable: Boolean,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> Color(0xFFAC2D00)
        !isAvailable -> Color(0xFFEEEEEE)
        else -> Color(0xFFF0E6E6)
    }
    val textColor = when {
        isSelected -> Color.White
        !isAvailable -> Color.Gray
        else -> Color(0xFFAC2D00)
    }
    Surface(
        modifier = modifier.clickable(enabled = isAvailable, onClick = onClick),
        shape = RoundedCornerShape(12.dp), color = bgColor
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)) {
            Icon(Icons.Default.TableBar, null, tint = textColor, modifier = Modifier.size(20.dp))
            Text("Bàn ${table.number}", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text("${table.capacity} người", color = textColor.copy(0.7f), fontSize = 10.sp)
            if (!isAvailable) {
                Text("Đã đặt", color = Color.Gray, fontSize = 9.sp)
            } else {
                Text("Trống", color = Color(0xFF2E7D32), fontSize = 9.sp)
            }
        }
    }
}
