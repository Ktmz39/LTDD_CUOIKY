package com.example.app_doublekrestaurant.ui.admin.profile

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.ui.auth.AuthViewModel
import com.example.app_doublekrestaurant.ui.admin.dashboard.AdminBottomNavigation
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToBooking: () -> Unit,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {}
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }
    val accentColor = Color(0xFFAC2D00)

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản Quản trị?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout { onLogout() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("Đăng xuất") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            AdminBottomNavigation(selectedTab = 4, onTabSelected = { 
                when(it) {
                    0 -> onNavigateToDashboard()
                    1 -> onNavigateToMenu()
                    2 -> onNavigateToBooking()
                    3 -> onNavigateToOrders()
                    4 -> { /* Current */ }
                }
            })
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(accentColor, Color(0xFFE64A19))))
                    .padding(bottom = 30.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        uri?.let { authViewModel.uploadAvatar(it) }
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
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
                                contentDescription = "Admin Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                (user?.fullName?.firstOrNull() ?: "A").toString(),
                                color = accentColor, fontWeight = FontWeight.Bold, fontSize = 40.sp
                            )
                        }
                        
                        if (authState.isUploadingImage) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(progress = { authState.uploadProgress }, color = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user?.fullName ?: "Admin", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(0.2f),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("👑 Administrator", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp).offset(y = (-15).dp)) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AdminProfileItem(Icons.Default.Email, "Email", user?.email ?: "N/A")
                        Divider(color = Color(0xFFF1F3F5), modifier = Modifier.padding(vertical = 12.dp))
                        AdminProfileItem(Icons.Default.Phone, "Số điện thoại", user?.phone ?: "Chưa cập nhật")
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("Quản lý Hệ thống", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                
                AdminMenuCard {
                    AdminMenuItem(Icons.Default.People, "Quản lý Người dùng", Color(0xFF1976D2)) { onNavigateToUsers() }
                    AdminMenuItem(Icons.Default.BarChart, "Báo cáo & Thống kê", Color(0xFF2E7D32)) { onNavigateToReports() }
                    AdminMenuItem(Icons.Default.VpnKey, "Đổi mật khẩu", Color(0xFFFFA000)) { onNavigateToChangePassword() }
                }

                Spacer(Modifier.height(20.dp))
                Text("Ứng dụng", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                
                AdminMenuCard {
                    AdminMenuItem(Icons.Default.Settings, "Cài đặt chung", Color(0xFF455A64))
                    AdminMenuItem(Icons.Default.Info, "Về ứng dụng", Color(0xFF78909C))
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDE8E4), contentColor = accentColor)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(12.dp))
                    Text("Đăng xuất", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AdminProfileItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AdminMenuCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) { Column(content = content) }
}

@Composable
fun AdminMenuItem(icon: ImageVector, title: String, color: Color, onClick: () -> Unit = {}) {
    Surface(onClick = onClick, color = Color.White) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}
