package com.example.app_doublekrestaurant.ui.user.cart

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.CartItem
import com.example.app_doublekrestaurant.util.formatVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCartScreen(
    viewModel: UserCartViewModel, // Passed from NavHost as shared VM
    onBack: () -> Unit,
    onCheckoutSuccess: () -> Unit
) {
    val items by viewModel.cartItems.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng (${viewModel.getTotalCount()})", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                CartBottomBar(
                    total = viewModel.getTotalPrice(),
                    itemCount = viewModel.getTotalCount(),
                    isLoading = uiState.isCheckingOut,
                    onCheckout = {
                        viewModel.checkout(onSuccess = onCheckoutSuccess)
                    }
                )
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (items.isEmpty()) {
            EmptyCartView(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.foodItem.id }) { item ->
                    CartItemRow(
                        item = item,
                        onUpdateQuantity = { viewModel.updateQuantity(item.foodItem.id, it) },
                        onRemove = { viewModel.removeFromCart(item.foodItem.id) }
                    )
                }
                item {
                    // Voucher Section
                    val claimedVouchers by viewModel.claimedVouchers.collectAsState()
                    var showVoucherSheet by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        var voucherCode by remember { mutableStateOf("") }
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Mã giảm giá", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (claimedVouchers.isNotEmpty()) {
                                    TextButton(onClick = { showVoucherSheet = true }) {
                                        Text("Chọn mã của bạn", color = Color(0xFFAC2D00), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = voucherCode,
                                    onValueChange = { voucherCode = it.uppercase() },
                                    placeholder = { Text("Nhập mã...", fontSize = 14.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFAC2D00),
                                        unfocusedBorderColor = Color(0xFFEEEEEE)
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.applyVoucher(voucherCode) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00).copy(0.1f), contentColor = Color(0xFFAC2D00)),
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                ) {
                                    Text("Áp dụng", fontWeight = FontWeight.Bold)
                                }
                            }
                            if (uiState.selectedVoucher != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Đã áp dụng: ${uiState.selectedVoucher?.code}", color = Color(0xFF2E7D32), fontSize = 13.sp)
                                    }
                                    TextButton(onClick = { viewModel.removeVoucher() }) {
                                        Text("Gỡ bỏ", color = Color.Red, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (showVoucherSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showVoucherSheet = false },
                            containerColor = Color.White
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Voucher của bạn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(Modifier.height(16.dp))
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(claimedVouchers) { v ->
                                        val mainColor = Color(0xFFE65100)
                                        val discountText = when (v.type) {
                                            "PERCENTAGE" -> "${v.discountPercentage}%"
                                            "GIFT" -> "Quà tặng"
                                            else -> "Giảm " + formatVnd(v.discountAmount)
                                        }
                                        Card(
                                            modifier = Modifier.fillMaxWidth().height(120.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            onClick = {
                                                viewModel.applyVoucher(v.code)
                                                showVoucherSheet = false
                                            }
                                        ) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                Box(
                                                    modifier = Modifier.weight(1f).fillMaxWidth().background(mainColor).padding(horizontal = 16.dp, vertical = 6.dp)
                                                ) {
                                                    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                                                        Text("Voucher $discountText", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                                    }
                                                }
                                                Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Box(modifier = Modifier.size(32.dp).background(Color(0xFFFFCCBC), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
                                                        val vector = when (v.iconType) {
                                                            "CONFETTI" -> Icons.Default.Celebration
                                                            "FIRE" -> Icons.Default.LocalFireDepartment
                                                            "GIFT" -> Icons.Default.CardGiftcard
                                                            else -> Icons.Default.ConfirmationNumber
                                                        }
                                                        Icon(vector, null, tint = mainColor, modifier = Modifier.size(16.dp))
                                                    }
                                                    Spacer(Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(v.title.ifEmpty { v.description }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black, maxLines = 1)
                                                        Text("Đơn tối thiểu ${formatVnd(v.minOrderAmount)}", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(32.dp))
                            }
                        }
                    }
                }

                item {
                    // Order summary card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Tóm tắt đơn hàng", fontWeight = FontWeight.Bold)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tạm tính (${items.sumOf { it.quantity }} món)", color = Color.Gray)
                                Text(formatVnd(viewModel.getSubtotal()), fontWeight = FontWeight.SemiBold)
                            }
                            if (uiState.discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Giảm giá", color = Color.Gray)
                                    Text("- ${formatVnd(uiState.discountAmount)}", color = Color(0xFFAC2D00), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Phí dịch vụ", color = Color.Gray)
                                Text("Miễn phí", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng cộng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(formatVnd(viewModel.getTotalPrice()), color = Color(0xFFAC2D00), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onUpdateQuantity: (Int) -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0E6E6)),
                contentAlignment = Alignment.Center
            ) {
                if (item.foodItem.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = item.foodItem.imageUrl,
                        contentDescription = item.foodItem.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Restaurant, null, tint = Color(0xFFAC2D00).copy(0.4f), modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.foodItem.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(formatVnd(item.foodItem.price), color = Color(0xFFAC2D00), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Tổng: ${formatVnd(item.foodItem.price * item.quantity)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFAC2D00), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF5F5F5)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        IconButton(onClick = { onUpdateQuantity(item.quantity - 1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, null, tint = Color(0xFFAC2D00), modifier = Modifier.size(16.dp))
                        }
                        Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(onClick = { onUpdateQuantity(item.quantity + 1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, null, tint = Color(0xFFAC2D00), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartBottomBar(total: Double, itemCount: Int, isLoading: Boolean, onCheckout: () -> Unit) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.background(Color.White).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng cộng ($itemCount món)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(formatVnd(total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFAC2D00))
                }
                Button(
                    onClick = onCheckout,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00)),
                    modifier = Modifier.height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.ShoppingBag, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đặt hàng ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Giỏ hàng trống", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        Text("Hãy chọn những món ăn yêu thích nhé!", color = Color.LightGray)
    }
}
