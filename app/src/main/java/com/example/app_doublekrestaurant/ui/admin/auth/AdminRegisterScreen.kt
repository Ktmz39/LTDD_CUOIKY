package com.example.app_doublekrestaurant.ui.admin.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.app_doublekrestaurant.data.model.UserRole
import com.example.app_doublekrestaurant.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRegisterScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adminCode by remember { mutableStateOf("") } // Special field for admin
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Đăng ký Admin", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Image(
                        painter = painterResource(id = com.example.app_doublekrestaurant.R.drawable.logo_doublek),
                        contentDescription = "Logo DoubleK",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Double K Admin",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAC2D00)
                    )
                    Text(
                        text = "Quản lý & Vận hành chuyên nghiệp",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            Text("Tạo tài khoản Quản trị", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Vui lòng nhập đầy đủ thông tin để quản lý nhà hàng", color = Color.Gray, fontSize = 14.sp)

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Tên quản trị viên *") },
                leadingIcon = { Icon(Icons.Default.Badge, null, tint = Color(0xFFAC2D00)) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFAC2D00), focusedLabelColor = Color(0xFFAC2D00))
            )
            
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email công việc *") },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFFAC2D00)) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFAC2D00), focusedLabelColor = Color(0xFFAC2D00))
            )

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Mật khẩu bảo mật *") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFAC2D00)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFAC2D00), focusedLabelColor = Color(0xFFAC2D00))
            )

            OutlinedTextField(
                value = adminCode, onValueChange = { adminCode = it },
                label = { Text("Mã kích hoạt Admin (tùy chọn)") },
                leadingIcon = { Icon(Icons.Default.Key, null, tint = Color(0xFFAC2D00)) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color(0xFFAC2D00), focusedLabelColor = Color(0xFFAC2D00))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.register(name.trim(), email.trim(), phone, password, UserRole.ADMIN) {
                        onRegisterSuccess()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00)),
                enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Đăng ký tài khoản Admin", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
