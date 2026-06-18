package com.example.app_doublekrestaurant.ui.admin.menu

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.util.formatVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen(
    viewModel: AdminMenuViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToBooking: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    authViewModel: com.example.app_doublekrestaurant.ui.auth.AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val accentColor = Color(0xFFAC2D00)
    var searchQuery by remember { mutableStateOf("") }
    val adminName = authState.currentUser?.fullName?.split(" ")?.lastOrNull() ?: "A"

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RestaurantMenu, null, tint = accentColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("DoubleK Admin", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.NotificationsNone, null)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        if (authState.currentUser?.avatarUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = authState.currentUser?.avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(accentColor, CircleShape),
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
        bottomBar = {
            AdminBottomNavigation(selectedTab = 1, onTabSelected = {
                when(it) {
                    0 -> onNavigateToDashboard()
                    2 -> onNavigateToBooking()
                    3 -> onNavigateToOrders()
                    4 -> onNavigateToProfile()
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd, containerColor = accentColor, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Add, null)
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header Section
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Quản lý Thực đơn", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Quản lý danh sách món ăn và trạng thái phục vụ", color = Color.Gray, fontSize = 14.sp)

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToAdd,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm món mới", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm món ăn...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = Color.LightGray,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Table Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF1F3F5)
            ) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Text("MÓN ĂN", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    // Cố định khoảng trống cho tiêu đề cột Giá tiền tương ứng với dòng dữ liệu bên dưới
                    Text("GIÁ TIỀN / THAO TÁC", modifier = Modifier.width(130.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.End)
                }
            }

            // Menu List
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.items.filter { it.name.contains(searchQuery, ignoreCase = true) }) { item ->
                        // ĐA SỬA LUỒNG: Đổi sang gọi viewModel.deleteItem theo đúng thiết kế của ViewModel
                        AdminMenuItemRow(
                            item = item,
                            accentColor = accentColor,
                            onClick = { onNavigateToEdit(item.id) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                        HorizontalDivider(color = Color(0xFFF1F3F5), modifier = Modifier.padding(horizontal = 20.dp))
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun AdminMenuItemRow(
    item: FoodItem,
    accentColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa món ăn", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa món '${item.name}' ra khỏi thực đơn nhà hàng không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("Xóa") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cụm nội dung thông tin bên trái (Ảnh + Tên + Danh mục) - Co dãn tự động linh hoạt
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Ảnh món ăn
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5))) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(model = item.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(Modifier.width(12.dp))

            // 2. Tên món ăn
            Column(modifier = Modifier.weight(1.2f).padding(end = 8.dp)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (item.isFeatured) {
                    Surface(color = Color(0xFFFFE0B2), shape = RoundedCornerShape(4.dp)) {
                        Text("BEST SELLER", color = Color(0xFFE65100), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }

            // 3. Tên danh mục món ăn
            Text(
                text = item.categoryName,
                modifier = Modifier.weight(0.8f),
                color = Color.Gray,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Cụm Giá tiền & Nút xóa bên phải - Khóa cứng kích thước 130.dp bảo đảm nút xóa thẳng lề tuyệt đối
        Row(
            modifier = Modifier.width(130.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatVnd(item.price).replace("₫", ""),
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f) // Đẩy văn bản giá tiền sát vào lề trái của cột 130.dp
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa món",
                    tint = Color.Gray.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
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