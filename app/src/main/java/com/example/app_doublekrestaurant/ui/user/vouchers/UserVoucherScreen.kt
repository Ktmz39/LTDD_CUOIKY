package com.example.app_doublekrestaurant.ui.user.vouchers

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.Voucher
import com.example.app_doublekrestaurant.ui.user.vouchers.UserVoucherViewModel
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.util.formatVnd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserVoucherScreen(
    onBack: () -> Unit,
    viewModel: UserVoucherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.isSuccess) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(uiState.error!!)
            viewModel.clearMessages()
        }
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Nhận mã giảm giá thành công!")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mã giảm giá", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        if (uiState.isLoading && uiState.vouchers.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
        } else {
            val activeVouchers = uiState.vouchers
            
            if (activeVouchers.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocalOffer, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Hiện chưa có mã giảm giá nào", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeVouchers, key = { it.id }) { voucher ->
                        UserVoucherCard(
                            voucher = voucher,
                            currentUser = uiState.currentUser,
                            onClaim = { viewModel.claimVoucher(voucher.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserVoucherCard(voucher: Voucher, currentUser: User?, onClaim: () -> Unit) {
    val isClaimed = currentUser?.claimedVouchers?.contains(voucher.id) == true
    val mainColor = Color(0xFFE65100)
    val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    val discountText = when (voucher.type) {
        "PERCENTAGE" -> "${voucher.discountPercentage}%"
        "GIFT" -> "Quà tặng"
        else -> "Giảm " + formatVnd(voucher.discountAmount)
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { if (!isClaimed) onClaim() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Half
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(mainColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                    Text("ƯU ĐÃI MỚI", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Voucher $discountText", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            
            // Bottom Half
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFFFCCBC), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val vector = when (voucher.iconType) {
                        "CONFETTI" -> Icons.Default.Celebration
                        "FIRE" -> Icons.Default.LocalFireDepartment
                        "GIFT" -> Icons.Default.CardGiftcard
                        else -> Icons.Default.ConfirmationNumber
                    }
                    Icon(vector, null, tint = mainColor, modifier = Modifier.size(20.dp))
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(voucher.title.ifEmpty { voucher.description }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black, maxLines = 1)
                    val subtitle = if (voucher.expiryDate > 0) "HSD: ${dateFormat.format(java.util.Date(voucher.expiryDate))}" else "Đơn tối thiểu ${formatVnd(voucher.minOrderAmount)}"
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                }
                
                Button(
                    onClick = onClaim,
                    enabled = !isClaimed,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isClaimed) Color(0xFFF5F5F5) else mainColor,
                        disabledContainerColor = Color(0xFFF5F5F5),
                        disabledContentColor = Color.Gray
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(if (isClaimed) "Đã nhận" else "Nhận", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isClaimed) Color.Gray else Color.White)
                }
            }
        }
    }
}
