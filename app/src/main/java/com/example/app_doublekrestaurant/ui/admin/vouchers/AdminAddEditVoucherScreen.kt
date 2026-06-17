package com.example.app_doublekrestaurant.ui.admin.vouchers

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
fun AdminAddEditVoucherScreen(
    voucherId: String?,
    viewModel: AdminAddEditVoucherViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val mainColor = Color(0xFFAC2D00)
    
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PERCENTAGE") } // PERCENTAGE, AMOUNT, GIFT
    var discountValue by remember { mutableStateOf("") } // Used for both % and amount
    var minOrder by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var expiryDate by remember { mutableStateOf(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000) }
    var selectedIcon by remember { mutableStateOf("TICKET") } // TICKET, CONFETTI, FIRE, GIFT

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var startDateStr by remember { mutableStateOf(dateFormat.format(Date(System.currentTimeMillis()))) }
    var expiryDateStr by remember { mutableStateOf(dateFormat.format(Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000))) }

    // Initialize state when voucher is loaded
    LaunchedEffect(voucherId) {
        if (voucherId != null) {
            viewModel.loadVoucher(voucherId)
        }
    }

    LaunchedEffect(uiState.voucher) {
        uiState.voucher?.let {
            title = it.title.ifEmpty { it.description }
            code = it.code
            type = it.type
            discountValue = if (it.type == "PERCENTAGE") it.discountPercentage.toString() else it.discountAmount.toInt().toString()
            minOrder = it.minOrderAmount.toInt().toString()
            maxDiscount = it.maxDiscountAmount.toInt().toString()
            usageLimit = it.usageLimit.toString()
            
            val stDate = if (it.startDate > 0) it.startDate else System.currentTimeMillis()
            val exDate = if (it.expiryDate > 0) it.expiryDate else System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000
            startDate = stDate
            expiryDate = exDate
            startDateStr = dateFormat.format(Date(stDate))
            expiryDateStr = dateFormat.format(Date(exDate))
            
            selectedIcon = it.iconType
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    val typeOptions = listOf("PERCENTAGE" to "Giảm giá %", "AMOUNT" to "Giảm tiền mặt", "GIFT" to "Quà tặng")
    var expandedType by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (voucherId == null) "Thêm Voucher" else "Sửa Voucher", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9F9F9),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Hủy bỏ", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val finalVoucher = (uiState.voucher ?: Voucher()).copy(
                                title = title,
                                description = title,
                                code = code.uppercase(),
                                type = type,
                                discountPercentage = if (type == "PERCENTAGE") discountValue.replace(",", "").replace(".", "").toIntOrNull() ?: 0 else 0,
                                discountAmount = if (type != "PERCENTAGE") discountValue.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0 else 0.0,
                                minOrderAmount = minOrder.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0,
                                maxDiscountAmount = maxDiscount.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0,
                                usageLimit = usageLimit.replace(",", "").replace(".", "").toIntOrNull() ?: 0,
                                startDate = startDate,
                                expiryDate = expiryDate,
                                iconType = selectedIcon,
                                isActive = true
                            )
                            viewModel.saveVoucher(finalVoucher)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = title.isNotBlank() && code.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (voucherId == null) "Tạo Voucher" else "Lưu thay đổi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Thông tin chương trình
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Thông tin chương trình", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Thiết lập các thông số cơ bản cho voucher mới của bạn.", color = Color.Gray, fontSize = 14.sp)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tên chương trình ưu đãi") },
                    placeholder = { Text("Ví dụ: Giảm giá mùa hè rực rỡ") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                )
                
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Mã Voucher (Code)") },
                    placeholder = { Text("Ví dụ: SUMMER20") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                )

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = typeOptions.find { it.first == type }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loại ưu đãi") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second) },
                                onClick = { type = option.first; expandedType = false }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text("Giá trị ưu đãi") },
                        placeholder = { Text("Ví dụ: 20") },
                        suffix = { Text(if (type == "PERCENTAGE") "%" else "VNĐ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                    )
                    
                    if (type == "PERCENTAGE") {
                        OutlinedTextField(
                            value = maxDiscount,
                            onValueChange = { maxDiscount = it },
                            label = { Text("Giảm tối đa (VNĐ)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = minOrder,
                        onValueChange = { minOrder = it },
                        label = { Text("Đơn hàng tối thiểu (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                    )
                    OutlinedTextField(
                        value = usageLimit,
                        onValueChange = { usageLimit = it },
                        label = { Text("Số lượng tối đa") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                    )
                }
            }

            // Section 2: Thời hạn sử dụng
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Thời hạn sử dụng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                OutlinedTextField(
                    value = startDateStr,
                    onValueChange = { 
                        startDateStr = it
                        try {
                            val parsed = dateFormat.parse(it)
                            if (parsed != null) startDate = parsed.time
                        } catch (e: Exception) {}
                    },
                    label = { Text("Ngày bắt đầu (dd/MM/yyyy)") },
                    trailingIcon = { 
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance().apply { timeInMillis = if (startDate > 0) startDate else System.currentTimeMillis() }
                            DatePickerDialog(context, { _, y, m, d ->
                                calendar.set(y, m, d)
                                startDate = calendar.timeInMillis
                                startDateStr = dateFormat.format(calendar.time)
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) {
                            Icon(Icons.Default.CalendarToday, null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                )

                OutlinedTextField(
                    value = expiryDateStr,
                    onValueChange = { 
                        expiryDateStr = it
                        try {
                            val parsed = dateFormat.parse(it)
                            if (parsed != null) {
                                // Set đến cuối ngày 23:59:59 để voucher hợp lệ suốt ngày
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = parsed.time
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                expiryDate = cal.timeInMillis
                            }
                        } catch (e: Exception) {}
                    },
                    label = { Text("Ngày kết thúc (dd/MM/yyyy)") },
                    trailingIcon = { 
                        IconButton(onClick = {
                            val calendar = Calendar.getInstance().apply { timeInMillis = if (expiryDate > 0) expiryDate else System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000 }
                            DatePickerDialog(context, { _, y, m, d ->
                                // Reset về cuối ngày 23:59:59 để voucher hợp lệ suốt cả ngày
                                calendar.set(y, m, d)
                                calendar.set(Calendar.HOUR_OF_DAY, 23)
                                calendar.set(Calendar.MINUTE, 59)
                                calendar.set(Calendar.SECOND, 59)
                                calendar.set(Calendar.MILLISECOND, 999)
                                expiryDate = calendar.timeInMillis
                                expiryDateStr = dateFormat.format(calendar.time)
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) {
                            Icon(Icons.Default.CalendarToday, null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE0E0E0))
                )
            }

            // Section 3: Hình ảnh & Icon
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Icon hiển thị", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val icons = listOf(
                        "TICKET" to Icons.Default.ConfirmationNumber,
                        "CONFETTI" to Icons.Default.Celebration,
                        "FIRE" to Icons.Default.LocalFireDepartment,
                        "GIFT" to Icons.Default.CardGiftcard
                    )
                    
                    icons.forEach { (iconName, vector) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedIcon == iconName) mainColor.copy(alpha = 0.1f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (selectedIcon == iconName) mainColor else Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = null,
                                tint = if (selectedIcon == iconName) mainColor else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Section 4: Xem trước hiển thị
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Xem trước hiển thị", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                VoucherPreviewCard(
                    title = title.ifEmpty { "Giảm giá mùa hè rực rỡ" },
                    subtitle = "HSD: ${dateFormat.format(Date(expiryDate))}",
                    iconType = selectedIcon,
                    discountText = if (type == "PERCENTAGE") "${discountValue.ifEmpty { "20" }}%" else "Giảm " + formatVnd(discountValue.toDoubleOrNull() ?: 20000.0)
                )
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun VoucherPreviewCard(
    title: String,
    subtitle: String,
    iconType: String,
    discountText: String
) {
    val mainColor = Color(0xFFE65100)
    
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    modifier = Modifier.size(40.dp).background(Color(0xFFFFCCBC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val vector = when (iconType) {
                        "CONFETTI" -> Icons.Default.Celebration
                        "FIRE" -> Icons.Default.LocalFireDepartment
                        "GIFT" -> Icons.Default.CardGiftcard
                        else -> Icons.Default.ConfirmationNumber
                    }
                    Icon(vector, null, tint = mainColor, modifier = Modifier.size(20.dp))
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                }
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5F5),
                    modifier = Modifier.height(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text("Lưu", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}
