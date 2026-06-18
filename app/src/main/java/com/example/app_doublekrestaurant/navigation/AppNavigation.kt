package com.example.app_doublekrestaurant.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.app_doublekrestaurant.data.model.UserRole
import com.example.app_doublekrestaurant.ui.user.cart.UserCartViewModel
import com.example.app_doublekrestaurant.ui.user.home.UserHomeScreen
import com.example.app_doublekrestaurant.ui.user.menu.UserMenuScreen
import com.example.app_doublekrestaurant.ui.admin.reports.AdminReportsScreen
import com.example.app_doublekrestaurant.ui.user.reviews.UserSubmitReviewScreen
import com.example.app_doublekrestaurant.ui.user.home.UserFoodDetailScreen
// ĐÃ MỞ KHÓA IMPORT: Kết nối thành công đến màn hình QR mới tạo
import com.example.app_doublekrestaurant.ui.user.payment.QRPaymentScreen

@Composable
fun AppNavigation(navController: NavHostController, startDestination: String) {
    val cartViewModel: UserCartViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
        exitTransition = { fadeOut(tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(250)) },
        popEnterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) },
        popExitTransition = { fadeOut(tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(250)) }
    ) {
        // ─── Auth Flow ────────────────────────────────────────────────
        composable(Screen.Login.route) {
            com.example.app_doublekrestaurant.ui.auth.LoginScreen(
                onLoginSuccess = { role ->
                    val dest = if (role == UserRole.ADMIN || role == UserRole.STAFF) Screen.AdminDashboard.route else Screen.UserHome.route
                    navController.navigate(dest) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToAdminRegister = { navController.navigate(Screen.AdminRegister.route) }
            )
        }
        composable(Screen.Register.route) {
            com.example.app_doublekrestaurant.ui.auth.RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.UserHome.route) { popUpTo(0) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminRegister.route) {
            com.example.app_doublekrestaurant.ui.admin.auth.AdminRegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.AdminDashboard.route) { popUpTo(0) { inclusive = true } } },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ChangePassword.route) {
            com.example.app_doublekrestaurant.ui.auth.ChangePasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ─── User Flow ────────────────────────────────────────────────
        composable(Screen.UserHome.route) {
            UserHomeScreen(
                cartViewModel = cartViewModel,
                onNavigateToMenu = { navController.navigate(Screen.UserMenu.route) },
                onNavigateToBooking = { navController.navigate(Screen.UserBooking.route) },
                onNavigateToCart = { navController.navigate(Screen.UserCart.route) },
                onNavigateToProfile = { navController.navigate(Screen.UserProfile.route) },
                onNavigateToOrders = { navController.navigate(Screen.UserOrders.route) },
                onNavigateToNotifications = { navController.navigate(Screen.UserNotifications.route) },
                onNavigateToSupport = { navController.navigate(Screen.UserSupport.route) },
                onNavigateToAIChat = { navController.navigate(Screen.UserAIChat.route) },
                onNavigateToReviews = { navController.navigate(Screen.UserReviewManagement.route) },
                onNavigateToVouchers = { navController.navigate(Screen.UserVouchers.route) },
                onNavigateToFoodDetail = { foodId -> navController.navigate(Screen.UserFoodDetail.createRoute(foodId)) }
            )
        }
        composable(Screen.UserVouchers.route) {
            com.example.app_doublekrestaurant.ui.user.vouchers.UserVoucherScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UserMenu.route) {
            UserMenuScreen(
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(Screen.UserCart.route) },
                onNavigateToFoodDetail = { foodId -> navController.navigate(Screen.UserFoodDetail.createRoute(foodId)) },
                onNavigateToAIChat = { navController.navigate(Screen.UserAIChat.route) }
            )
        }
        // ĐÃ MỞ KHÓA VÀ CẬP NHẬT: Nhận sự kiện chuyển hướng từ giỏ hàng sang mã QR kèm tổng tiền
        composable(Screen.UserCart.route) {
            com.example.app_doublekrestaurant.ui.user.cart.UserCartScreen(
                viewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onCheckoutSuccess = { navController.navigate(Screen.UserOrderStatus.route) { popUpTo(Screen.UserHome.route) } },
                onNavigateToQRPayment = { amount ->
                    navController.navigate(Screen.UserQRPayment.createRoute(amount))
                }
            )
        }
        // ĐÃ MỞ KHÓA TOÀN BỘ: Đăng ký màn hình QRPaymentScreen vào cây định tuyến hệ thống
        composable(
            route = Screen.UserQRPayment.route,
            arguments = listOf(navArgument("totalAmount") { type = NavType.LongType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getLong("totalAmount") ?: 0L
            QRPaymentScreen(
                totalAmount = amount,
                onPaymentConfirmed = {
                    // Thực thi logic cập nhật đơn hàng lên Firebase sau khi bấm xác nhận chuyển khoản
                    cartViewModel.checkout(
                        paymentMethod = com.example.app_doublekrestaurant.data.model.PaymentMethod.PAY_NOW,
                        onSuccess = {
                            navController.navigate(Screen.UserOrderStatus.route) {
                                popUpTo(Screen.UserHome.route)
                            }
                        }
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.UserOrderStatus.route) {
            SuccessScreen(title = "Đặt hàng thành công! 🎉", message = "Đơn hàng đã được gửi đến nhà hàng.\nChúng tôi sẽ xác nhận trong vài phút.", onAction = { navController.navigate(Screen.UserHome.route) { popUpTo(0) } })
        }
        composable(Screen.UserBooking.route) {
            com.example.app_doublekrestaurant.ui.user.booking.TableBookingScreen(onBack = { navController.popBackStack() }, onSuccess = { navController.navigate("booking_success") { popUpTo(Screen.UserHome.route) } })
        }
        composable("booking_success") {
            SuccessScreen(title = "Đặt bàn thành công! 🍽️", message = "Yêu cầu đặt bàn đã được gửi.\nNhà hàng sẽ xác nhận và liên hệ với bạn.", onAction = { navController.navigate(Screen.UserHome.route) { popUpTo(0) } })
        }
        composable(Screen.UserProfile.route) {
            com.example.app_doublekrestaurant.ui.user.profile.UserProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onNavigateToReviews = { navController.navigate(Screen.UserReviewManagement.route) },
                onNavigateToReservations = { navController.navigate(Screen.UserReservations.route) },
                onNavigateToOrders = { navController.navigate(Screen.UserOrders.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) }
            )
        }
        composable(Screen.UserOrders.route) {
            com.example.app_doublekrestaurant.ui.user.orders.UserOrderHistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToReview = { orderId ->
                    navController.navigate(Screen.UserSubmitReview.createRoute(orderId))
                }
            )
        }
        composable(
            route = Screen.UserSubmitReview.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            UserSubmitReviewScreen(
                orderId = orderId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.UserReservations.route) {
            com.example.app_doublekrestaurant.ui.user.booking.UserReservationHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UserNotifications.route) {
            com.example.app_doublekrestaurant.ui.user.notifications.UserNotificationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UserSupport.route) {
            com.example.app_doublekrestaurant.ui.user.support.UserSupportScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UserAIChat.route) {
            com.example.app_doublekrestaurant.ui.user.chat.AIChatScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UserReviewManagement.route) {
            com.example.app_doublekrestaurant.ui.user.reviews.UserReviewManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.UserFoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            UserFoodDetailScreen(
                foodId = foodId,
                onBack = { navController.popBackStack() },
                onNavigateToAIChat = { navController.navigate(Screen.UserAIChat.route) }
            )
        }

        // ─── Admin Flow ───────────────────────────────────────────────
        composable(Screen.AdminDashboard.route) {
            com.example.app_doublekrestaurant.ui.admin.dashboard.AdminDashboardScreen(
                onNavigateToMenu = { navController.navigate(Screen.AdminMenuManagement.route) },
                onNavigateToOrders = { navController.navigate(Screen.AdminOrderManagement.route) },
                onNavigateToStats = { navController.navigate(Screen.AdminReports.route) },
                onNavigateToBooking = { navController.navigate(Screen.AdminBookingManagement.route) },
                onNavigateToTables = { navController.navigate(Screen.AdminTableManagement.route) },
                onNavigateToUsers = { navController.navigate(Screen.AdminUserManagement.route) },
                onNavigateToVouchers = { navController.navigate(Screen.AdminVoucherManagement.route) },
                onNavigateToReviews = { navController.navigate(Screen.AdminReviews.route) },
                onNavigateToSupport = { navController.navigate(Screen.AdminSupport.route) },
                onNavigateToNotifications = { navController.navigate(Screen.AdminNotifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.AdminProfile.route) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }
        composable(Screen.AdminReports.route) {
            AdminReportsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminMenuManagement.route) {
            com.example.app_doublekrestaurant.ui.admin.menu.AdminMenuScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate(Screen.AdminAddEditFood.createRoute("new")) },
                onNavigateToEdit = { foodId -> navController.navigate(Screen.AdminAddEditFood.createRoute(foodId)) },
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToOrders = { navController.navigate(Screen.AdminOrderManagement.route) },
                onNavigateToBooking = { navController.navigate(Screen.AdminBookingManagement.route) },
                onNavigateToProfile = { navController.navigate(Screen.AdminProfile.route) }
            )
        }
        composable(
            route = Screen.AdminAddEditFood.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")
            com.example.app_doublekrestaurant.ui.admin.menu.AdminAddEditFoodScreen(
                foodId = if (foodId == "new") null else foodId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminOrderManagement.route) { com.example.app_doublekrestaurant.ui.admin.orders.AdminOrderManagementScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminBookingManagement.route) { com.example.app_doublekrestaurant.ui.admin.booking.AdminBookingManagementScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminTableManagement.route) { com.example.app_doublekrestaurant.ui.admin.tables.AdminTableManagementScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminUserManagement.route) { com.example.app_doublekrestaurant.ui.admin.users.AdminUserManagementScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminVoucherManagement.route) {
            com.example.app_doublekrestaurant.ui.admin.vouchers.AdminVoucherManagementScreen(
                onBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate(Screen.AdminAddEditVoucher.createRoute("new")) },
                onNavigateToEdit = { voucherId -> navController.navigate(Screen.AdminAddEditVoucher.createRoute(voucherId)) }
            )
        }
        composable(
            route = Screen.AdminAddEditVoucher.route,
            arguments = listOf(navArgument("voucherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val voucherId = backStackEntry.arguments?.getString("voucherId")
            com.example.app_doublekrestaurant.ui.admin.vouchers.AdminAddEditVoucherScreen(
                voucherId = if (voucherId == "new") null else voucherId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminStatistics.route) { com.example.app_doublekrestaurant.ui.admin.stats.AdminStatisticsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminReviews.route) { com.example.app_doublekrestaurant.ui.admin.reviews.AdminReviewManagementScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminSupport.route) {
            com.example.app_doublekrestaurant.ui.admin.support.AdminSupportScreen(
                onBack = { navController.popBackStack() },
                onNavigateToChat = { roomId -> navController.navigate(Screen.AdminChat.createRoute(roomId)) }
            )
        }
        composable(
            route = Screen.AdminChat.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            com.example.app_doublekrestaurant.ui.admin.support.AdminChatScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminNotifications.route) { com.example.app_doublekrestaurant.ui.admin.notifications.AdminNotificationsScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.AdminProfile.route) {
            com.example.app_doublekrestaurant.ui.admin.profile.AdminProfileScreen(
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                onNavigateToDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                onNavigateToMenu = { navController.navigate(Screen.AdminMenuManagement.route) },
                onNavigateToOrders = { navController.navigate(Screen.AdminOrderManagement.route) },
                onNavigateToBooking = { navController.navigate(Screen.AdminBookingManagement.route) },
                onNavigateToUsers = { navController.navigate(Screen.AdminUserManagement.route) },
                onNavigateToReports = { navController.navigate(Screen.AdminReports.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) }
            )
        }
    }
}

@Composable
fun SuccessScreen(title: String, message: String, onAction: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(100.dp))
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(message, color = Color.Gray, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        Spacer(Modifier.height(48.dp))
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAC2D00))) {
            Text("Về trang chủ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}