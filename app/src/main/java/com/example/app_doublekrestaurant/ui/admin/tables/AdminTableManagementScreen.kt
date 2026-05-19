package com.example.app_doublekrestaurant.ui.admin.tables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.app_doublekrestaurant.data.model.RestaurantTable
import com.example.app_doublekrestaurant.data.model.TableStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTableManagementScreen(
    viewModel: AdminTableViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var tableToEdit by remember { mutableStateOf<RestaurantTable?>(null) }

    if (showAddDialog || tableToEdit != null) {
        TableDialog(
            table = tableToEdit,
            onDismiss = { showAddDialog = false; tableToEdit = null },
            onConfirm = { table ->
                viewModel.addOrUpdateTable(table)
                showAddDialog = false
                tableToEdit = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý bàn ăn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFAC2D00),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.tables.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.tables) { table ->
                    TableAdminCard(
                        table = table,
                        onEdit = { tableToEdit = table },
                        onDelete = { viewModel.deleteTable(table.id) },
                        onToggle = { viewModel.toggleAvailability(table) }
                    )
                }
            }
        }
    }
}

@Composable
fun TableAdminCard(
    table: RestaurantTable,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val (statusText, statusColor, containerColor) = when (table.tableStatus) {
        TableStatus.AVAILABLE -> Triple("Trống", Color(0xFF2E7D32), Color.White)
        TableStatus.RESERVED -> Triple("Đã đặt trước", Color(0xFF1976D2), Color(0xFFE3F2FD))
        TableStatus.OCCUPIED -> Triple("Đang dùng", Color(0xFF7B1FA2), Color(0xFFEDE7F6))
        TableStatus.UNAVAILABLE -> Triple("Không khả dụng", Color.Gray, Color(0xFFF5F5F5))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onEdit
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bàn ${table.number}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${table.capacity} chỗ", fontSize = 14.sp, color = Color.Gray)
            Text("Tầng ${table.floor}", fontSize = 12.sp, color = Color.Gray)
            
            Spacer(Modifier.height(8.dp))
            
            AssistChip(
                onClick = onToggle,
                label = { Text(statusText) },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = statusColor
                )
            )
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun TableDialog(
    table: RestaurantTable?,
    onDismiss: () -> Unit,
    onConfirm: (RestaurantTable) -> Unit
) {
    var number by remember { mutableStateOf(table?.number?.toString() ?: "") }
    var capacity by remember { mutableStateOf(table?.capacity?.toString() ?: "2") }
    var floor by remember { mutableStateOf(table?.floor?.toString() ?: "1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (table == null) "Thêm bàn mới" else "Sửa bàn ${table.number}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Số bàn") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Số người tối đa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Tầng") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val n = number.toIntOrNull() ?: 0
                val c = capacity.toIntOrNull() ?: 2
                val f = floor.toIntOrNull() ?: 1
                onConfirm(table?.copy(number = n, capacity = c, floor = f) ?: RestaurantTable(number = n, capacity = c, floor = f))
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
