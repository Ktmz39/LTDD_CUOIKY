package com.example.app_doublekrestaurant.ui.admin.support

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.data.model.SupportRoom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportScreen(
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: AdminSupportViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    authViewModel: com.example.app_doublekrestaurant.ui.auth.AuthViewModel = hiltViewModel()
) {
    val mainColor = Color(0xFFAC2D00)
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val adminName = authState.currentUser?.fullName?.split(" ")?.lastOrNull() ?: "A"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DoubleK", color = mainColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Support", fontWeight = FontWeight.Light, fontSize = 24.sp)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = mainColor) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, null) }
                    IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, null) }
                    IconButton(onClick = onNavigateToProfile) {
                        if (authState.currentUser?.avatarUrl?.isNotEmpty() == true) {
                            coil3.compose.AsyncImage(
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
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Header
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Hộp thư", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm kiếm khách hàng...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainColor,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5)
                    )
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = mainColor)
                }
            } else {
                val filteredRooms = uiState.chatRooms.filter {
                    it.userName.contains(searchQuery, ignoreCase = true) || it.lastMessage.contains(searchQuery, ignoreCase = true)
                }
                
                // Chat List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredRooms) { room ->
                        val timeStr = formatTime(room.lastMessageTime)
                        ChatItem(
                            name = room.userName,
                            lastMsg = room.lastMessage,
                            time = timeStr,
                            avatarUrl = room.userAvatarUrl,
                            hasUnread = room.unreadCountAdmin > 0,
                            onClick = { onNavigateToChat(room.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    name: String, 
    lastMsg: String, 
    time: String, 
    avatarUrl: String, 
    hasUnread: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasUnread) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).background(Color(0xFFAC2D00), RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(16.dp))
        }
        
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color(0xFFF5F5F5)
        ) {
            if (avatarUrl.isNotBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, null, modifier = Modifier.padding(10.dp), tint = Color.LightGray)
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold, fontSize = 15.sp)
                Text(time, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    lastMsg,
                    fontSize = 13.sp,
                    color = if (hasUnread) Color.Black else Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (hasUnread) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFAC2D00), CircleShape))
                }
            }
        }
    }
}

private fun formatTime(timeInMillis: Long): String {
    if (timeInMillis == 0L) return ""
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date(timeInMillis))
}
