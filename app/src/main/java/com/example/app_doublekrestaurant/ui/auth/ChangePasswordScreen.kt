package com.example.app_doublekrestaurant.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val authState by authViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val accentColor = Color(0xFFAC2D00)
    val textDark = Color(0xFF2D3142)
    val textGray = Color(0xFF9098B1)
    val bgLight = Color(0xFFF8F9FA)

    // Clear ViewModel error when initialized
    LaunchedEffect(Unit) {
        authViewModel.clearError()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = bgLight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header description
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier
                        .size(64.dp)
                        .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bảo mật tài khoản",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vui lòng nhập mật khẩu cũ và thiết lập mật khẩu mới có độ bảo mật cao (tối thiểu 6 ký tự).",
                    fontSize = 14.sp,
                    color = textGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(30.dp))

                // Card containing the form fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Password
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Mật khẩu hiện tại",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { 
                                    oldPassword = it
                                    validationError = null 
                                    authViewModel.clearError()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("********", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = textGray) },
                                trailingIcon = {
                                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                        Icon(
                                            imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = textGray
                                        )
                                    }
                                },
                                visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color(0xFFEBF0F0)
                                ),
                                singleLine = true
                            )
                        }

                        // New Password
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Mật khẩu mới",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { 
                                    newPassword = it
                                    validationError = null 
                                    authViewModel.clearError()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("********", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = textGray) },
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = textGray
                                        )
                                    }
                                },
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color(0xFFEBF0F0)
                                ),
                                singleLine = true
                            )
                        }

                        // Confirm New Password
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Xác nhận mật khẩu mới",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = textDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    validationError = null 
                                    authViewModel.clearError()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("********", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = textGray) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = textGray
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color(0xFFEBF0F0)
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Error message
                val displayError = validationError ?: authState.error
                if (displayError != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(displayError, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                            validationError = "Vui lòng nhập đầy đủ các trường"
                            return@Button
                        }
                        if (newPassword.length < 6) {
                            validationError = "Mật khẩu mới phải có tối thiểu 6 ký tự"
                            return@Button
                        }
                        if (newPassword == oldPassword) {
                            validationError = "Mật khẩu mới không được trùng mật khẩu hiện tại"
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            validationError = "Mật khẩu mới và mật khẩu xác nhận không khớp"
                            return@Button
                        }

                        authViewModel.changePassword(
                            oldPassword = oldPassword,
                            newPassword = newPassword,
                            onSuccess = {
                                successMessage = "Đổi mật khẩu thành công!"
                                scope.launch {
                                    delay(2000)
                                    onBack()
                                }
                            },
                            onFailure = { err ->
                                // Handled via state and shown using displayError
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = !authState.isLoading
                ) {
                    if (authState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Cập nhật mật khẩu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Custom Animated Success Overlay/Dialog
            if (successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Thành công!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = successMessage!!,
                                fontSize = 14.sp,
                                color = textGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
