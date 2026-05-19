package com.example.app_doublekrestaurant.ui.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.ui.auth.AuthViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.util.CloudinaryService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToReviews: () -> Unit = {},
    onNavigateToReservations: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {}
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout { onLogout() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00))
                ) { Text("Đăng xuất") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFAC2D00), titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFFAC2D00), Color(0xFFFF7043))))
                    .padding(bottom = 40.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        uri?.let { authViewModel.uploadAvatar(it) }
                    }

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(enabled = !authState.isUploadingImage) { 
                                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.avatarUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                (user?.fullName?.firstOrNull() ?: "U").toString(),
                                color = Color(0xFFAC2D00), fontWeight = FontWeight.Bold, fontSize = 36.sp
                            )
                        }
                        
                        // Loading overlay or subtle camera hint at bottom
                        if (authState.isUploadingImage) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { authState.uploadProgress },
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        } else {
                            // Small camera hint at bottom
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(0.35f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(user?.fullName ?: "Người dùng", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("4.8 (Thực khách tin cậy)", color = Color.White.copy(0.9f), fontSize = 13.sp)
                    }
                    Text(user?.email ?: "", color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodyMedium)
                    if (user?.phone?.isNotEmpty() == true) {
                        Text(user.phone, color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(0.2f)
                    ) {
                        Text(
                            when (user?.role?.name) {
                                "ADMIN" -> "👑 Quản trị viên"
                                "STAFF" -> "⭐ Nhân viên"
                                else -> "🍽️ Thực khách"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {

                    Text("Tài khoản", style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    ProfileMenuCard {
                        ProfileMenuItem("Thông tin cá nhân", Icons.Default.Person, Color(0xFF1976D2))
                        ProfileMenuItem("Đổi mật khẩu", Icons.Default.Lock, Color(0xFFFFA000)) { onNavigateToChangePassword() }
                        ProfileMenuItem("Địa chỉ đã lưu", Icons.Default.LocationOn, Color(0xFF2E7D32))
                        ProfileMenuItem("Phương thức thanh toán", Icons.Default.CreditCard, Color(0xFFAC2D00))
                    }

                    Text("Hoạt động", style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    ProfileMenuCard {
                        ProfileMenuItem("Lịch sử đơn hàng", Icons.Default.History, Color(0xFF7B1FA2)) { onNavigateToOrders() }
                        ProfileMenuItem("Đơn đặt bàn", Icons.Default.TableBar, Color(0xFFFFA000)) { onNavigateToReservations() }
                        ProfileMenuItem("Đánh giá của tôi", Icons.Default.StarRate, Color(0xFFFF7043)) { onNavigateToReviews() }
                    }

                    Text("Cài đặt", style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                    ProfileMenuCard {
                        ProfileMenuItem("Thông báo", Icons.Default.Notifications, Color(0xFF00796B))
                        ProfileMenuItem("Ngôn ngữ", Icons.Default.Language, Color(0xFF455A64))
                        ProfileMenuItem("Trợ giúp & Hỗ trợ", Icons.Default.HelpOutline, Color(0xFF795548))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        onClick = { showLogoutDialog = true }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, null, tint = Color(0xFFAC2D00))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Đăng xuất", color = Color(0xFFAC2D00), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileMenuCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) { Column(content = content) }
}

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, iconColor: Color, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = Color.White) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}
