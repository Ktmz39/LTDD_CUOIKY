package com.example.app_doublekrestaurant.ui.admin.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import coil3.compose.AsyncImage
import com.example.app_doublekrestaurant.util.CloudinaryService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditFoodScreen(
    foodId: String? = null,
    viewModel: AdminAddEditFoodViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(foodId) {
        foodId?.let { viewModel.loadFoodItem(it) }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (foodId == null) "Thêm món ăn" else "Sửa món ăn", fontWeight = FontWeight.Bold) },
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
        if (uiState.isLoading && foodId != null && uiState.name.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAC2D00))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Picker Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        uri?.let { viewModel.uploadImage(it) }
                    }

                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                            .clickable(enabled = !uiState.isUploadingImage) { 
                                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = uiState.imageUrl,
                                contentDescription = "Food Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddCircle, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        }

                        if (uiState.isUploadingImage) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { uiState.uploadProgress },
                                    color = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    Text(
                        if (uiState.isUploadingImage) "Đang tải ảnh... ${(uiState.uploadProgress * 100).toInt()}%" 
                        else "Nhấn để chọn ảnh từ thiết bị", 
                        fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Tên món ăn") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = viewModel::onPriceChange,
                    label = { Text("Giá (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text("Danh mục", fontWeight = FontWeight.SemiBold)
                uiState.categories.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = uiState.categoryId == category.id,
                            onClick = { viewModel.onCategoryChange(category.id) }
                        )
                        Text(category.name)
                    }
                }

                HorizontalDivider()

                ToggleRow("Còn hàng", uiState.isAvailable, viewModel::onAvailableChange)
                ToggleRow("Món nổi bật", uiState.isFeatured, viewModel::onFeaturedChange)
                ToggleRow("Cay", uiState.isSpicy, viewModel::onSpicyChange)
                ToggleRow("Món chay", uiState.isVegetarian, viewModel::onVegetarianChange)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = viewModel::saveFoodItem,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00)),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(if (foodId == null) "Thêm món ăn" else "Lưu thay đổi")
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
