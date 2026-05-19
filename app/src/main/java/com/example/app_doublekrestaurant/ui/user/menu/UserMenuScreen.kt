package com.example.app_doublekrestaurant.ui.user.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.FoodItem
import com.example.app_doublekrestaurant.ui.user.cart.UserCartViewModel
import com.example.app_doublekrestaurant.util.formatVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuScreen(
    viewModel: UserMenuViewModel = hiltViewModel(),
    cartViewModel: UserCartViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToFoodDetail: (String) -> Unit = {},
    onNavigateToAIChat: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartCount = cartItems.sumOf { it.quantity }
    val snackbarHostState = remember { SnackbarHostState() }
    
    var addedToCartTrigger by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(addedToCartTrigger) {
        if (addedToCartTrigger > 0) {
            kotlinx.coroutines.delay(2000)
            addedToCartTrigger = 0
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Thực đơn", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    actions = {
                        BadgedBox(badge = { if (cartCount > 0) Badge { Text("$cartCount") } }) {
                            IconButton(onClick = onNavigateToCart) { Icon(Icons.Default.ShoppingCart, null, tint = Color(0xFFAC2D00)) }
                        }
                        Spacer(Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                // Search Bar
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.search(it) },
                        placeholder = { Text("Tìm món...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.search("") }) {
                                    Icon(Icons.Default.Clear, null, tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                // Category Tabs
                if (uiState.categories.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = if (uiState.selectedCategoryId == null) 0
                        else (uiState.categories.indexOfFirst { it.id == uiState.selectedCategoryId } + 1).coerceAtLeast(0),
                        edgePadding = 16.dp,
                        containerColor = Color.White,
                        contentColor = Color(0xFFAC2D00),
                        divider = {}
                    ) {
                        Tab(
                            selected = uiState.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            text = { Text("Tất cả", fontSize = 13.sp) }
                        )
                        uiState.categories.forEach { cat ->
                            Tab(
                                selected = uiState.selectedCategoryId == cat.id,
                                onClick = { viewModel.selectCategory(cat.id) },
                                text = { Text(cat.name, fontSize = 13.sp) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAIChat,
                containerColor = Color(0xFFAC2D00),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, "AI Chat", modifier = Modifier.size(24.dp))
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
            uiState.filteredItems.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (uiState.searchQuery.isNotEmpty()) "Không tìm thấy \"${uiState.searchQuery}\""
                        else "Chưa có món ăn nào",
                        color = Color.Gray, textAlign = TextAlign.Center
                    )
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("${uiState.filteredItems.size} món ăn", fontSize = 13.sp, color = Color.Gray)
                }
                items(uiState.filteredItems, key = { it.id }) { item ->
                    MenuItemCard(
                        item = item,
                        onAddToCart = {
                            cartViewModel.addToCart(item)
                            addedToCartTrigger++
                        },
                        onClick = { onNavigateToFoodDetail(item.id) }
                    )
                }
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
}

@Composable
fun MenuItemCard(item: FoodItem, onAddToCart: () -> Unit, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0E6E6)),
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
                    Icon(Icons.Default.Restaurant, null, tint = Color(0xFFAC2D00).copy(0.4f), modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    if (item.isSpicy) Text("🌶️", fontSize = 13.sp)
                    if (item.isVegetarian) Text("🥦", fontSize = 13.sp)
                }
                if (item.description.isNotEmpty()) {
                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 2)
                }
                if (item.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000), modifier = Modifier.size(14.dp))
                        Text(" ${String.format("%.1f", item.rating)} (${item.reviewCount})", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(formatVnd(item.price), color = Color(0xFFAC2D00), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    FilledIconButton(
                        onClick = onAddToCart,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFAC2D00))
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
