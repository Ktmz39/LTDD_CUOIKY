package com.example.app_doublekrestaurant.ui.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.Review
import com.example.app_doublekrestaurant.util.formatVnd
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFoodDetailScreen(
    foodId: String,
    onBack: () -> Unit,
    onNavigateToAIChat: () -> Unit = {},
    viewModel: UserFoodDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val food = uiState.foodItem
    val mainColor = Color(0xFFAC2D00)
    val snackbarHostState = remember { SnackbarHostState() }
    
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(foodId) {
        viewModel.loadFoodDetail(foodId)
    }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
        uiState.successMessage?.let { 
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            comment = ""
            rating = 5
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(food?.name ?: "Chi tiết món ăn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAIChat,
                containerColor = Color(0xFFAC2D00),
                contentColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.AutoAwesome, "AI Chat")
            }
        }
    ) { padding ->
        if (uiState.isLoading && food == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = mainColor)
            }
        } else if (food == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy thông tin món ăn")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                // Food Image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        if (food.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = food.imageUrl,
                                contentDescription = food.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Restaurant, null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                            }
                        }
                    }
                }

                // Food Info
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(food.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Surface(
                                color = Color(0xFFFDF1EB),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFA000), modifier = Modifier.size(16.dp))
                                    Text(" ${food.rating}", fontWeight = FontWeight.Bold, color = mainColor)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        Text(formatVnd(food.price), color = mainColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(Modifier.height(16.dp))
                        Text("Mô tả", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            food.description.ifEmpty { "Món ăn ngon tuyệt được chế biến từ những nguyên liệu tươi sạch nhất tại DoubleK Restaurant." },
                            color = Color.Gray,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                item { Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFEEEEEE)) }

                // Add Review Section
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Đánh giá của bạn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            repeat(5) { index ->
                                IconButton(onClick = { rating = index + 1 }) {
                                    Icon(
                                        if (index < rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        null,
                                        tint = if (index < rating) Color(0xFFFFA000) else Color.LightGray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            placeholder = { Text("Mời bạn để lại bình luận về món ăn...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mainColor,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.submitReview(rating, comment) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                            enabled = !uiState.isSubmittingReview
                        ) {
                            if (uiState.isSubmittingReview) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Gửi đánh giá", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Divider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFEEEEEE)) }

                // Review List Section
                item {
                    Text(
                        "Đánh giá từ khách hàng (${uiState.reviews.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                if (uiState.reviews.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(bottom = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Chưa có đánh giá nào cho món ăn này.", color = Color.Gray)
                        }
                    }
                } else {
                    items(uiState.reviews) { review ->
                        FoodReviewItem(review)
                    }
                }
                
                item { Spacer(Modifier.height(30.dp)) }
            }
        }
    }
}

@Composable
fun FoodReviewItem(review: Review) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(review.createdAt))
    
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                if (review.userAvatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = review.userAvatarUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(review.userName.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFFAC2D00))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(dateStr, color = Color.Gray, fontSize = 11.sp)
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Row {
            repeat(5) { index ->
                Icon(
                    if (index < review.rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                    null,
                    tint = if (index < review.rating) Color(0xFFFFA000) else Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        if (review.comment.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(review.comment, fontSize = 14.sp, color = Color.DarkGray)
        }
        
        Spacer(Modifier.height(12.dp))
        Divider(color = Color(0xFFF5F5F5))
    }
}
