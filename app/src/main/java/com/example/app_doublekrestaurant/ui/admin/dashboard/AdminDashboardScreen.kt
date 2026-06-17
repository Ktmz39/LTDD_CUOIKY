package com.example.app_doublekrestaurant.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.*
import com.example.app_doublekrestaurant.util.formatVnd
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onNavigateToOrders: () -> Unit,
    onNavigateToBooking: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToVouchers: () -> Unit,
    onNavigateToStats: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    authViewModel: com.example.app_doublekrestaurant.ui.auth.AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val adminName = authState.currentUser?.fullName?.split(" ")?.lastOrNull() ?: "A"
    val accentColor = Color(0xFFAC2D00)
    var selectedTab by remember { mutableIntStateOf(0) }
    var isTableLayoutExpanded by remember { mutableStateOf(false) }
    var showRevenueDetailsSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                // Sidebar Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF5C1A00), Color(0xFFAC2D00), Color(0xFFD4601A))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).background(Color.White.copy(0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("DoubleK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("Admin Panel", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        }
                    }
                }
                // Admin info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (authState.currentUser?.avatarUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = authState.currentUser?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp).background(accentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(adminName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(authState.currentUser?.fullName ?: "Admin", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Quản trị viên", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(8.dp))

                // Admin Nav Items
                AdminDrawerNavItem(Icons.Default.Home, "Trang chủ", accentColor) { scope.launch { drawerState.close() } }
                AdminDrawerNavItem(Icons.Default.RestaurantMenu, "Quản lý thực đơn", accentColor) { scope.launch { drawerState.close() }; onNavigateToMenu() }
                AdminDrawerNavItem(Icons.Default.ReceiptLong, "Quản lý đơn hàng", accentColor) { scope.launch { drawerState.close() }; onNavigateToOrders() }
                AdminDrawerNavItem(Icons.Default.TableBar, "Quản lý đặt bàn", accentColor) { scope.launch { drawerState.close() }; onNavigateToBooking() }
                AdminDrawerNavItem(Icons.Default.GridView, "Quản lý bàn", accentColor) { scope.launch { drawerState.close() }; onNavigateToTables() }
                AdminDrawerNavItem(Icons.Default.People, "Quản lý người dùng", accentColor) { scope.launch { drawerState.close() }; onNavigateToUsers() }
                AdminDrawerNavItem(Icons.Default.ConfirmationNumber, "Vouchers", accentColor) { scope.launch { drawerState.close() }; onNavigateToVouchers() }
                AdminDrawerNavItem(Icons.Default.Star, "Đánh giá", accentColor) { scope.launch { drawerState.close() }; onNavigateToReviews() }
                AdminDrawerNavItem(Icons.Default.BarChart, "Thống kê", accentColor) { scope.launch { drawerState.close() }; onNavigateToStats() }
                AdminDrawerNavItem(Icons.Default.Chat, "Hỗ trợ", accentColor) { scope.launch { drawerState.close() }; onNavigateToSupport() }

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                AdminDrawerNavItem(Icons.Default.Person, "Hồ sơ", accentColor) { scope.launch { drawerState.close() }; onNavigateToProfile() }
                AdminDrawerNavItem(Icons.Default.Logout, "Đăng xuất", Color.Red) { scope.launch { drawerState.close() }; onLogout() }
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF5C1A00), Color(0xFFAC2D00), Color(0xFFD4601A))
                        )
                    )
                    .statusBarsPadding()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("DoubleK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onNavigateToSupport) {
                        Icon(Icons.Default.Chat, null, tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        if (authState.currentUser?.avatarUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = authState.currentUser?.avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color.White.copy(0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(adminName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AdminBottomNavigation(selectedTab = 0, onTabSelected = { 
                when(it) {
                    1 -> onNavigateToMenu()
                    2 -> onNavigateToBooking()
                    3 -> onNavigateToOrders()
                    4 -> onNavigateToProfile()
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new something */ },
                containerColor = accentColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Greeting
            Text("Chào Admin!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Dưới đây là tóm tắt hoạt động của nhà hàng ngày hôm nay.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            
            // Quick Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNavigateToVouchers,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tạo Voucher", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onNavigateToUsers,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Người dùng", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(20.dp))

            // Statistics Grid
            SummaryCard(
                title = "Doanh thu hôm nay",
                value = formatVnd(uiState.totalRevenue),
                icon = Icons.Default.Payments,
                iconBg = Color(0xFFFDE8E4),
                badge = "Xem chi tiết",
                badgeColor = Color(0xFFFDE8E4),
                badgeTextColor = accentColor,
                onClick = { showRevenueDetailsSheet = true }
            )
            
            Spacer(Modifier.height(12.dp))
            
            SummaryCard(
                title = "Số đơn hàng mới",
                value = "${uiState.pendingOrdersCount}",
                icon = Icons.Default.ReceiptLong,
                iconBg = Color(0xFFE8EAF6),
                badge = "Xử lý ngay",
                badgeColor = Color(0xFFE8EAF6),
                badgeTextColor = Color(0xFF3F51B5),
                onClick = onNavigateToOrders
            )

            Spacer(Modifier.height(12.dp))
            
            SummaryCard(
                title = "Số bàn đang phục vụ",
                value = "${uiState.activeTablesCount} / ${uiState.totalTableCount}",
                icon = Icons.Default.TableBar,
                iconBg = Color(0xFFFFF3E0),
                badge = "${if (uiState.totalTableCount > 0) (uiState.activeTablesCount * 100 / uiState.totalTableCount) else 0}% Công suất",
                badgeColor = Color(0xFFF5F5F5),
                badgeTextColor = Color.Gray,
                onClick = { isTableLayoutExpanded = !isTableLayoutExpanded },
                expandableContent = {
                    androidx.compose.animation.AnimatedVisibility(visible = isTableLayoutExpanded) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                            Divider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(bottom = 16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sơ đồ bàn", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Quản lý", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToTables() })
                            }
                            Spacer(Modifier.height(16.dp))
                            
                            if (uiState.tables.isEmpty()) {
                                Text("Chưa có dữ liệu bàn", color = Color.Gray, fontSize = 14.sp)
                            } else {
                                val tableRows = uiState.tables.chunked(4)
                                tableRows.forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        row.forEach { table ->
                                            val isAvailable = table.isAvailable
                                            val bgColor = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFDE8E4)
                                            val textColor = if (isAvailable) Color(0xFF2E7D32) else accentColor
                                            
                                            Surface(
                                                modifier = Modifier.weight(1f).aspectRatio(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = bgColor
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(4.dp)
                                                ) {
                                                    Text("${table.number}", fontWeight = FontWeight.Bold, color = textColor, fontSize = 16.sp)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        if (isAvailable) "Trống" else "Kín", 
                                                        color = textColor, 
                                                        fontSize = 11.sp, 
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // Weekly Revenue Chart Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRevenueDetailsSheet = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tăng trưởng doanh thu tuần", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("7 ngày qua", fontSize = 11.sp)
                                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                    // Chart Placeholder using real values
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxRevenue = uiState.weeklyRevenue.maxOrNull() ?: 1.0
                        uiState.weeklyRevenue.forEach { daily ->
                            val barHeight = (daily / maxRevenue * 80).toInt().coerceAtLeast(5)
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(barHeight.dp)
                                    .background(accentColor.copy(0.6f), RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        for (day in days) {
                            Text(day, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Best Sellers
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Món bán chạy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    if (uiState.topSellingItems.isEmpty()) {
                        Text("Chưa có dữ liệu bán hàng", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        uiState.topSellingItems.forEach { item ->
                            BestSellerItem(item.name, "${item.orderCount} đơn hàng", formatVnd(item.totalRevenue), item.imageUrl)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToMenu,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.3f))
                    ) {
                        Text("Xem chi tiết", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Orders Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Đơn hàng cần xử lý", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Xem tất cả", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToOrders() })
                    }
                    Spacer(Modifier.height(16.dp))
                    // Table Header
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Mã đơn", modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Gray)
                        Text("Khách hàng", modifier = Modifier.weight(1.5f), fontSize = 11.sp, color = Color.Gray)
                        Text("Bàn", modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color.Gray)
                        Text("Trạng thái", modifier = Modifier.weight(1.2f), fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.End)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF5F5F5))
                    
                    if (uiState.ordersToProcess.isEmpty()) {
                        Text("Không có đơn hàng mới", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp))
                    } else {
                        uiState.ordersToProcess.forEach { order ->
                            OrderRow(
                                id = "#GS-${order.id.takeLast(4).uppercase()}",
                                customer = order.userName,
                                table = if (order.tableNumber > 0) "Bàn ${order.tableNumber}" else "Mang về",
                                status = when(order.status) {
                                    OrderStatus.PENDING.name -> "Đang chờ"
                                    OrderStatus.CONFIRMED.name -> "Xác nhận"
                                    OrderStatus.PREPARING.name -> "Chế biến"
                                    else -> order.status
                                },
                                statusColor = when(order.status) {
                                    OrderStatus.PENDING.name -> accentColor
                                    OrderStatus.CONFIRMED.name -> Color(0xFF1976D2)
                                    OrderStatus.PREPARING.name -> Color(0xFFFFA000)
                                    else -> Color.Gray
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    } // end ModalNavigationDrawer

    if (showRevenueDetailsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRevenueDetailsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Báo cáo Doanh thu",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    IconButton(onClick = { showRevenueDetailsSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
                
                HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(vertical = 12.dp))
                
                // Monthly Revenue Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E4).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFFDE8E4), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, null, tint = accentColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Tổng doanh thu tháng này",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = formatVnd(uiState.monthlyRevenue),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = accentColor
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Beautiful Revenue Chart (Last 7 Days Trend)
                Text(
                    text = "Xu hướng Doanh thu 7 ngày qua",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF2D3142),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val maxRevenue = uiState.dailyRevenueDetails.maxOfOrNull { it.revenue } ?: 1.0
                        val maxRevenueVal = if (maxRevenue > 0) maxRevenue else 1.0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            uiState.dailyRevenueDetails.forEach { detail ->
                                val barHeight = (detail.revenue / maxRevenueVal * 80).toInt().coerceAtLeast(6)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Tooltip or small revenue number on top of the bar
                                    if (detail.revenue > 0) {
                                        val shortText = if (detail.revenue >= 1_000_000) {
                                            String.format("%.1fM", detail.revenue / 1_000_000)
                                        } else if (detail.revenue >= 1_000) {
                                            String.format("%.0fk", detail.revenue / 1_000)
                                        } else {
                                            String.format("%.0f", detail.revenue)
                                        }
                                        Text(
                                            text = shortText,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "0",
                                            fontSize = 9.sp,
                                            color = Color.LightGray,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    
                                    // Visual Bar
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height(barHeight.dp)
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(accentColor, accentColor.copy(alpha = 0.5f))
                                                ),
                                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Date Label
                                    Text(
                                        text = detail.date,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Title
                Text(
                    text = "Doanh thu từng ngày trong tháng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF2D3142),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Detailed Day List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.monthlyDailyRevenueDetails.isEmpty()) {
                            Text(
                                text = "Chưa có doanh thu nào trong tháng này",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            uiState.monthlyDailyRevenueDetails.forEach { detail ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ngày ${detail.date}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF2D3142)
                                        )
                                    }
                                    Text(
                                        text = formatVnd(detail.revenue),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (detail.revenue > 0) Color(0xFF2E7D32) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String, 
    value: String, 
    icon: ImageVector, 
    iconBg: Color, 
    badge: String, 
    badgeColor: Color, 
    badgeTextColor: Color,
    onClick: (() -> Unit)? = null,
    expandableContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
    ) {
        Column {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color(0xFFAC2D00).copy(0.7f), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.Gray, fontSize = 13.sp)
                    Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Surface(color = badgeColor, shape = RoundedCornerShape(8.dp)) {
                    Text(badge, color = badgeTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            if (expandableContent != null) {
                expandableContent()
            }
        }
    }
}

@Composable
fun BestSellerItem(name: String, count: String, price: String, imageUrl: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5))) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(count, color = Color.Gray, fontSize = 12.sp)
        }
        Text(price, color = Color(0xFFAC2D00).copy(0.7f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun OrderRow(id: String, customer: String, table: String, status: String, statusColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(id, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(customer, modifier = Modifier.weight(1.5f), fontSize = 13.sp)
        Text(table, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Surface(
            color = statusColor.copy(0.1f), 
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.weight(1.2f)
        ) {
            Text(status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun AdminBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val tabs = listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Thực đơn", Icons.Default.RestaurantMenu, 1),
            Triple("Đặt bàn", Icons.Default.TableBar, 2),
            Triple("Đơn hàng", Icons.Default.ReceiptLong, 3),
            Triple("Cá nhân", Icons.Default.PersonOutline, 4)
        )
        for (tab in tabs) {
            val (label, icon, index) = tab
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, label) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFAC2D00),
                    selectedTextColor = Color(0xFFAC2D00),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFAC2D00).copy(0.1f)
                )
            )
        }
    }
}

@Composable
fun AdminDrawerNavItem(icon: ImageVector, label: String, accentColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
        }
    }
}
