package com.example.app_doublekrestaurant.ui.admin.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    viewModel: AdminUserViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý người dùng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFAC2D00),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.users) { user ->
                    UserAdminCard(
                        user = user,
                        onRoleChange = { role -> viewModel.updateUserRole(user.uid, role) },
                        onToggleActive = { active -> viewModel.toggleUserActive(user.uid, active) },
                        onDelete = { viewModel.deleteUser(user.uid) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserAdminCard(
    user: User,
    onRoleChange: (String) -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFAC2D00).copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.fullName,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(user.fullName.firstOrNull()?.toString() ?: "U", color = Color(0xFFAC2D00), fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(user.email, fontSize = 12.sp, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when(user.role) {
                        UserRole.ADMIN -> Color(0xFFE8F5E9)
                        UserRole.STAFF -> Color(0xFFE3F2FD)
                        else -> Color(0xFFF5F5F5)
                    }
                ) {
                    Text(
                        user.role.name,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = when(user.role) {
                            UserRole.ADMIN -> Color(0xFF2E7D32)
                            UserRole.STAFF -> Color(0xFF1976D2)
                            else -> Color.Gray
                        }
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Lên Admin") },
                        onClick = { onRoleChange("ADMIN"); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Lên Nhân viên") },
                        onClick = { onRoleChange("STAFF"); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Về Người dùng") },
                        onClick = { onRoleChange("USER"); showMenu = false }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (user.isActive) "Khóa tài khoản" else "Mở tài khoản") },
                        onClick = { onToggleActive(!user.isActive); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Xóa", color = Color.Red) },
                        onClick = { onDelete(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}
