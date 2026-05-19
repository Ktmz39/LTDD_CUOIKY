package com.example.app_doublekrestaurant.ui.admin.reports

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
import com.example.app_doublekrestaurant.ui.admin.reviews.AdminReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onBack: () -> Unit,
    reviewViewModel: AdminReviewViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    authViewModel: com.example.app_doublekrestaurant.ui.auth.AuthViewModel = hiltViewModel()
) {
    val reviews by reviewViewModel.reviews.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val adminName = authState.currentUser?.fullName?.split(" ")?.lastOrNull() ?: "A"
    val avgRating = if (reviews.isEmpty()) 0.0 else reviews.sumOf { it.rating }.toDouble() / reviews.size
    val mainColor = Color(0xFFAC2D00)
    var selectedPeriod by remember { mutableStateOf("30 ngày qua") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DoubleK", color = mainColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Reports", fontWeight = FontWeight.Light, fontSize = 24.sp)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = mainColor) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null) }
                    IconButton(onClick = onNavigateToProfile) {
                        if (authState.currentUser?.avatarUrl?.isNotEmpty() == true) {
                            coil3.compose.AsyncImage(
                                model = authState.currentUser?.avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(mainColor, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = adminName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Báo cáo Đánh giá", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Phân tích chi tiết phản hồi của khách hàng và hiệu suất dịch vụ.", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(Modifier.height(24.dp))
            
            // Period Selector
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    PeriodButton("30 ngày qua", selectedPeriod == "30 ngày qua") { selectedPeriod = it }
                    PeriodButton("Quý này", selectedPeriod == "Quý này") { selectedPeriod = it }
                    PeriodButton("Năm nay", selectedPeriod == "Năm nay") { selectedPeriod = it }
                    IconButton(onClick = {}) { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(20.dp)) }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Avg Rating Card
            ReportMetricCard(
                title = "ĐIỂM TRUNG BÌNH",
                value = String.format("%.1f", avgRating),
                subtitle = "/ 5.0",
                trend = "+0.2 so với tháng trước",
                trendColor = Color(0xFF2E7D32),
                icon = { 
                    Box(modifier = Modifier.size(64.dp).background(Color(0xFFFDF1EB), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFAC2D00), modifier = Modifier.size(32.dp))
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // Total Reviews Card
            ReportMetricCard(
                title = "TỔNG ĐÁNH GIÁ",
                value = "${reviews.size}",
                subtitle = "Lượt nhận xét",
                icon = { Box(Modifier.size(40.dp)) }
            )

            Spacer(Modifier.height(16.dp))

            // Satisfaction Rate
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TỈ LỆ HÀI LÒNG", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("85%", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFFAC2D00))
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = 0.85f,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFAC2D00),
                        trackColor = Color(0xFFF5F5F5)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Trend Section (Simulated)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Xu hướng đánh giá", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Color(0xFFAC2D00), androidx.compose.foundation.shape.CircleShape))
                            Text(" Số lượng", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).background(Color(0xFFFFA000), androidx.compose.foundation.shape.CircleShape))
                            Text(" Điểm số", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("Biểu đồ xu hướng đang tải...", color = Color.LightGray, fontSize = 14.sp)
                    }
                }
            }
            
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun PeriodButton(label: String, isSelected: Boolean, onClick: (String) -> Unit) {
    TextButton(
        onClick = { onClick(label) },
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isSelected) Color(0xFFAC2D00) else Color.Gray
        )
    ) {
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
    }
}

@Composable
fun ReportMetricCard(
    title: String,
    value: String,
    subtitle: String = "",
    trend: String = "",
    trendColor: Color = Color.Gray,
    icon: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(20.dp))
            Column {
                Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color.DarkGray)
                    if (subtitle.isNotEmpty()) {
                        Text(" $subtitle", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
                if (trend.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = trendColor, modifier = Modifier.size(14.dp))
                        Text(" $trend", color = trendColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
