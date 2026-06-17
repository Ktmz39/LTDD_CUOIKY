package com.example.app_doublekrestaurant.ui.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.ui.user.cart.UserCartViewModel
import com.example.app_doublekrestaurant.util.formatVnd
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    viewModel: UserHomeViewModel = hiltViewModel(),
    cartViewModel: UserCartViewModel,
    onNavigateToMenu: () -> Unit,
    onNavigateToBooking: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToAIChat: () -> Unit = {},
    onNavigateToReviews: () -> Unit = {},
    onNavigateToVouchers: () -> Unit = {},
    onNavigateToFoodDetail: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }
    var selectedTab by remember { mutableIntStateOf(0) }

    val greetingHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when {
        greetingHour < 12 -> "Chào buổi sáng"
        greetingHour < 18 -> "Chào buổi chiều"
        else -> "Chào buổi tối"
    }
    val userName = uiState.user?.fullName?.split(" ")?.lastOrNull() ?: "bạn"

    val mainColor = Color(0xFFAC2D00)
    val darkBg = Color(0xFF0F2027)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var addedToCartTrigger by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(addedToCartTrigger) {
        if (addedToCartTrigger > 0) {
            kotlinx.coroutines.delay(2000)
            addedToCartTrigger = 0
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                // Sidebar Header - Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF5C1A00), Color(0xFFAC2D00), Color(0xFFD4601A))
                            )
                        )
                        .padding(24.dp)
                        .clickable {
                            scope.launch { drawerState.close() }
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Restaurant, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("DoubleK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Text("Restaurant", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        }
                    }
                }

                // User info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.user?.avatarUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = uiState.user?.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp).background(mainColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(userName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(uiState.user?.fullName ?: "Khách", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(greeting, color = Color.Gray, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))
                Spacer(Modifier.height(8.dp))

                // Navigation Items
                DrawerNavItem(Icons.Default.Home, "Trang chủ", mainColor) {
                    scope.launch { drawerState.close() }
                }
                DrawerNavItem(Icons.Default.RestaurantMenu, "Thực đơn", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToMenu()
                }
                DrawerNavItem(Icons.Default.TableBar, "Đặt bàn", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToBooking()
                }
                DrawerNavItem(Icons.Default.ShoppingCart, "Giỏ hàng", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToCart()
                }
                DrawerNavItem(Icons.Default.ReceiptLong, "Lịch sử đơn hàng", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToOrders()
                }
                DrawerNavItem(Icons.Default.ConfirmationNumber, "Vouchers", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToVouchers()
                }
                DrawerNavItem(Icons.Default.Star, "Đánh giá", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToReviews()
                }
                DrawerNavItem(Icons.Default.Chat, "Hỗ trợ", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToSupport()
                }
                DrawerNavItem(Icons.Default.AutoAwesome, "AI Assistant", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToAIChat()
                }

                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = Color(0xFFF0F0F0))
                DrawerNavItem(Icons.Default.Person, "Cá nhân", mainColor) {
                    scope.launch { drawerState.close() }; onNavigateToProfile()
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
    Scaffold(
        topBar = {
            // Gradient header matching web design
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
                    // Hamburger menu
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                    // DoubleK Logo - clickable (already on home, just close drawer)
                    Row(
                        modifier = Modifier.clickable { /* Already on home */ },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Restaurant, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("DoubleK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Spacer to push right actions to end
                    Spacer(Modifier.weight(1f))

                    // Right actions: chat + notification + cart + avatar
                    IconButton(onClick = onNavigateToSupport) {
                        Icon(Icons.Default.Chat, null, tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White)
                    }
                    Box {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
                        }
                        if (cartCount > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                                containerColor = Color.Yellow,
                                contentColor = Color.Black
                            ) {
                                Text(cartCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        if (uiState.user?.avatarUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = uiState.user?.avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color.White.copy(0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(userName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            UserBottomNavigation(selectedIndex = selectedTab) { index ->
                selectedTab = index
                when (index) {
                    1 -> onNavigateToMenu()
                    2 -> onNavigateToBooking()
                    3 -> onNavigateToCart()
                    4 -> onNavigateToProfile()
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAIChat,
                containerColor = Color(0xFFAC2D00),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, "AI Chat", modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AI Assistant", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = mainColor)
                }
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // Search Bar
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp)
                            .clickable { onNavigateToMenu() },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F2F2)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Tìm kiếm món ăn, nhà hàng...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }

                // Promo Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFAC2D00))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("ƯU ĐÃI ĐẶC BIỆT", color = Color.White.copy(0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Giảm 20% cho đơn hàng đầu tiên",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                )
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = onNavigateToVouchers,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFAC2D00)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Nhận ngay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.popularItems.isNotEmpty()) {
                                    AsyncImage(
                                        model = uiState.popularItems.first().imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Actions
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f).height(120.dp).clickable { onNavigateToBooking() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF2F2F2)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(Color(0xFFEBE0DD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TableBar, null, tint = mainColor)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Đặt bàn", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f).height(120.dp).clickable { onNavigateToMenu() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF2F2F2)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(Color(0xFFEBE0DD), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Restaurant, null, tint = mainColor)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Đặt món", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gợi ý cho bạn", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onNavigateToMenu) {
                            Text("Xem tất cả", color = mainColor, fontSize = 12.sp)
                        }
                    }
                }

                // Suggestion Items
                items(uiState.popularItems) { item ->
                    VerticalFoodCard(
                        item = item, 
                        onAddClick = { 
                            cartViewModel.addToCart(item) 
                            addedToCartTrigger++
                        },
                        onClick = { onNavigateToFoodDetail(item.id) }
                    )
                }
                
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
        
        androidx.compose.animation.AnimatedVisibility(
            visible = addedToCartTrigger > 0,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = Color(0xFFAC2D00).copy(0.9f),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Đã thêm vào giỏ hàng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        }
    }
    } // end ModalNavigationDrawer
}


@Composable
fun VerticalFoodCard(item: FoodItem, onAddClick: () -> Unit, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF0F0F0))
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Restaurant, null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
                    }
                }
                
                // Rating chip top right
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    color = Color.White.copy(0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(item.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatVnd(item.price),
                        color = Color(0xFFAC2D00),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFAC2D00), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun UserBottomNavigation(selectedIndex: Int = 0, onTabSelected: (Int) -> Unit = {}) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        listOf(
            Triple("Trang chủ", Icons.Default.Home, 0),
            Triple("Thực đơn", Icons.Default.RestaurantMenu, 1),
            Triple("Đặt bàn", Icons.Default.TableBar, 2),
            Triple("Giỏ hàng", Icons.Default.ShoppingCart, 3),
            Triple("Cá nhân", Icons.Default.PersonOutline, 4)
        ).forEach { (label, icon, index) ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, null) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFAC2D00),
                    selectedTextColor = Color(0xFFAC2D00),
                    indicatorColor = Color(0xFFAC2D00).copy(0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun DrawerNavItem(icon: ImageVector, label: String, accentColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
