package com.example.app_doublekrestaurant.ui.user.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.Order
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSubmitReviewScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: UserReviewViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    authViewModel: com.example.app_doublekrestaurant.ui.auth.AuthViewModel = hiltViewModel()
) {
    val order by viewModel.selectedOrder.collectAsState()
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var showThankYou by remember { mutableStateOf(false) }

    LaunchedEffect(showThankYou) {
        if (showThankYou) {
            kotlinx.coroutines.delay(2000)
            onBack()
        }
    }
    val mainColor = Color(0xFFAC2D00)
    val authState by authViewModel.uiState.collectAsState()
    val userName = authState.currentUser?.fullName?.split(" ")?.lastOrNull() ?: "B"

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DoubleK", color = mainColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Review", fontWeight = FontWeight.Light, fontSize = 24.sp)
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.Close, null, tint = mainColor) 
                    } 
                },
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
                                    text = userName.take(1).uppercase(),
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
        if (order == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = mainColor)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Experience Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Trải nghiệm của bạn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(16.dp))
                        
                        order!!.items.firstOrNull()?.let { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(60.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF5F5F5)
                                ) {
                                    if (item.imageUrl.isNotEmpty()) {
                                        coil3.compose.AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = item.name,
                                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Restaurant, null, modifier = Modifier.padding(12.dp), tint = Color.LightGray)
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Đơn hàng #${order!!.id.take(8).uppercase()}", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Thời gian", color = Color.Gray, fontSize = 12.sp)
                                Text(SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(Date(order!!.createdAt)), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Nhân viên phục vụ", color = Color.Gray, fontSize = 12.sp)
                                Text("DoubleK Team", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Incentive Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFDF1EB),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalOffer, null, tint = Color(0xFFAC2D00))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "ƯU ĐÃI: Nhận ngay 50 điểm thưởng và voucher 10% cho lần sau khi hoàn thành đánh giá này!",
                            color = Color(0xFFAC2D00),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(30.dp))

                // Rating Section
                Text(
                    "Bạn cảm thấy dịch vụ như thế nào?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }, modifier = Modifier.size(56.dp)) {
                            Icon(
                                if (index < rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                null,
                                tint = if (index < rating) Color(0xFFFFA000) else Color.LightGray,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
                
                Text(
                    when(rating) {
                        1 -> "Rất tệ"
                        2 -> "Không hài lòng"
                        3 -> "Bình thường"
                        4 -> "Hài lòng"
                        else -> "Rất hài lòng"
                    },
                    color = Color(0xFFAC2D00),
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(32.dp))

                // Feedback Box
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Chia sẻ chi tiết cảm nhận của bạn", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Mời bạn nhập đánh giá...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mainColor,
                            unfocusedBorderColor = Color(0xFFEEEEEE),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(Modifier.height(40.dp))

                // Submit Button
                Button(
                    onClick = { 
                        order!!.items.forEach { item ->
                            viewModel.submitReview(item.foodItemId, item.name, rating, comment)
                        }
                        showThankYou = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor)
                ) {
                    Text("Gửi đánh giá ngay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(Modifier.height(20.dp))
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showThankYou,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        color = Color(0xFFAC2D00).copy(0.9f),
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(16.dp))
                            Text("Cảm ơn bạn đã đánh giá!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
