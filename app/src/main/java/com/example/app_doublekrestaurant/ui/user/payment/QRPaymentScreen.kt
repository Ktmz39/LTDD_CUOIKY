package com.example.app_doublekrestaurant.ui.user.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRPaymentScreen(
    totalAmount: Long,
    onPaymentConfirmed: () -> Unit,
    onBack: () -> Unit
) {
    // 1. Cấu hình thông tin ngân hàng của bạn tại đây để nhận tiền
    val bankId = "mbbank" // Thay bằng mã ngân hàng của bạn (ví dụ: vcb, mbbank, icb...)
    val accountNo = "0911816562" // Thay bằng số tài khoản ngân hàng thật của bạn
    val accountName = "Tran Huynh Anh Kiet" // Thay bằng tên chủ tài khoản (Viết hoa không dấu)
    val memo = "DoubleK Restaurant Thanh Toan" // Nội dung chuyển khoản

    // 2. Link API tự động tạo mã VietQR theo chuẩn quốc gia
    val qrUrl = "https://img.vietqr.io/image/$bankId-$accountNo-compact2.jpg?amount=$totalAmount&addInfo=$memo&accountName=$accountName"

    // Định dạng hiển thị tiền tệ VND (Ví dụ: 150.000 đ)
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val formattedAmount = formatter.format(totalAmount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán QR", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quét mã để thanh toán",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formattedAmount,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAC2D00)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Hiển thị ảnh QR được tải tự động từ VietQR API dựa trên số tiền của giỏ hàng
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = qrUrl),
                            contentDescription = "VietQR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Chủ TK: $accountName\nSTK: $accountNo ($bankId)",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 20.sp
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "*Sau khi chuyển khoản thành công, vui lòng bấm nút xác nhận bên dưới để hệ thống ghi nhận đơn hàng.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onPaymentConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00))
                ) {
                    Text("Tôi đã chuyển khoản thành công", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}