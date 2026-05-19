package com.example.app_doublekrestaurant.ui.admin.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.Review
import com.example.app_doublekrestaurant.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AdminReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()
    val isLoading = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            reviewRepository.getReviews().collect {
                _reviews.value = it.sortedByDescending { r -> r.createdAt }
                isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewManagementScreen(
    viewModel: AdminReviewViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    authViewModel: com.example.app_doublekrestaurant.ui.auth.AuthViewModel = hiltViewModel()
) {
    val reviews by viewModel.reviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val adminName = authState.currentUser?.fullName?.split(" ")?.lastOrNull() ?: "A"
    val avgRating = if (reviews.isEmpty()) 0.0 else reviews.sumOf { it.rating }.toDouble() / reviews.size
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var selectedTime by remember { mutableStateOf("7 ngày qua") }

    val mainColor = Color(0xFFAC2D00)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DoubleK", color = mainColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Reviews", fontWeight = FontWeight.Light, fontSize = 24.sp)
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.Close, null, tint = mainColor) 
                    } 
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, null) }
                    IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null) }
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
                                    .background(mainColor, CircleShape),
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
        containerColor = Color(0xFFFBFBFB)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header Section
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Quản lý Đánh giá", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Theo dõi và phản hồi ý kiến khách hàng để cải thiện dịch vụ.", color = Color.Gray, fontSize = 14.sp)
                
                Spacer(Modifier.height(20.dp))
                
                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Trung bình", fontSize = 12.sp, color = Color.Gray)
                            Text(String.format("%.1f ★", avgRating), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tổng đánh giá", fontSize = 12.sp, color = Color.Gray)
                            Text("${reviews.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }

            // Filters Section
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                color = Color(0xFFFDF5F2),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterDropdown(
                            label = "Lọc theo sao",
                            value = selectedFilter,
                            icon = Icons.Default.FilterList,
                            modifier = Modifier.weight(1f)
                        )
                        FilterDropdown(
                            label = "Thời gian",
                            value = selectedTime,
                            icon = Icons.Default.CalendarToday,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tên khách hàng...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mainColor,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = mainColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reviews.filter { it.userName.contains(searchQuery, true) }) { review ->
                        PremiumReviewCard(review)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterDropdown(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Column {
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumReviewCard(review: Review) {
    val dateStr = remember(review.createdAt) {
        val diff = System.currentTimeMillis() - review.createdAt
        when {
            diff < 3600000 -> "${diff / 60000} phút trước"
            diff < 86400000 -> "${diff / 3600000} giờ trước"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(review.createdAt))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.userAvatarUrl.isNotEmpty()) {
                        AsyncImage(
                            model = review.userAvatarUrl,
                            contentDescription = review.userName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5)
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.padding(8.dp), tint = Color.LightGray)
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFF1EB),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("MỚI NHẤT", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFFAC2D00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                if (i < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                null,
                                tint = if (i < review.rating) Color(0xFFFFA000) else Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(dateStr, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "“${review.comment}”",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color.DarkGray,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            if (review.foodItemName.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text(review.foodItemName, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
