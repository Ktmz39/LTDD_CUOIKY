package com.example.app_doublekrestaurant.ui.admin.vouchers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.Voucher
import com.example.app_doublekrestaurant.util.formatVnd
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVoucherManagementScreen(
    viewModel: AdminVoucherViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val mainColor = Color(0xFFAC2D00)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Lỗi: $it")
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DoubleK", color = mainColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Vouchers", fontWeight = FontWeight.Light, fontSize = 24.sp)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = mainColor) } },
                actions = {
                    IconButton(onClick = onNavigateToAdd) { Icon(Icons.Default.Add, null, tint = mainColor) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Summary Header
            Surface(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                color = Color(0xFFFDF5F2),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).background(mainColor, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ConfirmationNumber, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Mã khuyến mãi", fontSize = 12.sp, color = Color.Gray)
                        val activeCount = uiState.vouchers.count { it.isActive }
                        Text("$activeCount mã đang hoạt động", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            if (uiState.isLoading && uiState.vouchers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = mainColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.vouchers) { voucher ->
                        PremiumVoucherAdminCard(
                            voucher = voucher,
                            onEdit = { onNavigateToEdit(voucher.id) },
                            onDelete = { viewModel.deleteVoucher(voucher.id) },
                            onToggle = { viewModel.toggleVoucher(voucher) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumVoucherAdminCard(
    voucher: Voucher,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (voucher.isActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        voucher.code,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = if (voucher.isActive) Color(0xFF2E7D32) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.weight(1f))

                // ĐÃ FIX: Bao bọc Switch bằng một Box cấu hình để tránh nuốt sự kiện onClick của Card
                Switch(
                    checked = voucher.isActive,
                    onCheckedChange = { _ -> onToggle() }, // Bỏ qua giá trị boolean truyền xuống để gọi trực tiếp hàm xử lý toggle của ViewModel
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFAC2D00)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(voucher.title.ifEmpty { voucher.description }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            if (voucher.description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(voucher.description, fontSize = 14.sp, color = Color.DarkGray)
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    val discountText = when (voucher.type) {
                        "PERCENTAGE" -> "${voucher.discountPercentage}%"
                        "GIFT" -> "Quà tặng"
                        else -> formatVnd(voucher.discountAmount)
                    }
                    Text(
                        "Giảm: $discountText",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFAC2D00)
                    )
                    Text("Đơn tối thiểu: ${formatVnd(voucher.minOrderAmount)}", fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun VoucherDialog(
    voucher: Voucher?,
    onDismiss: () -> Unit,
    onConfirm: (Voucher) -> Unit
) {
    var code by remember { mutableStateOf(voucher?.code ?: "") }
    var description by remember { mutableStateOf(voucher?.description ?: "") }
    var discountAmount by remember { mutableStateOf(voucher?.discountAmount?.toInt()?.toString() ?: "0") }
    var discountPercentage by remember { mutableStateOf(voucher?.discountPercentage?.toString() ?: "0") }
    var minOrder by remember { mutableStateOf(voucher?.minOrderAmount?.toInt()?.toString() ?: "0") }
    var maxDiscount by remember { mutableStateOf(voucher?.maxDiscountAmount?.toInt()?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (voucher == null) "Thêm Voucher" else "Sửa Voucher", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Mã Voucher") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = discountAmount, onValueChange = { discountAmount = it }, label = { Text("Số tiền giảm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = discountPercentage, onValueChange = { discountPercentage = it }, label = { Text("% Giảm") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = minOrder, onValueChange = { minOrder = it }, label = { Text("Đơn tối thiểu") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = maxDiscount, onValueChange = { maxDiscount = it }, label = { Text("Giảm tối đa") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalVoucher = (voucher ?: Voucher()).copy(
                        code = code,
                        description = description,
                        discountAmount = discountAmount.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0,
                        discountPercentage = discountPercentage.replace(",", "").replace(".", "").toIntOrNull() ?: 0,
                        minOrderAmount = minOrder.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0,
                        maxDiscountAmount = maxDiscount.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
                    )
                    onConfirm(finalVoucher)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) } }
    )
}