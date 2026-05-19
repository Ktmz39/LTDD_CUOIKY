package com.example.app_doublekrestaurant.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminRegister : Screen("admin_register")
    object ChangePassword : Screen("change_password")

    // User Flow
    object UserHome : Screen("user_home")
    object UserMenu : Screen("user_menu")
    object UserCart : Screen("user_cart")
    object UserBooking : Screen("user_booking")
    object UserOrderStatus : Screen("user_order_status")
    object UserProfile : Screen("user_profile")
    object UserReviews : Screen("user_reviews")
    object UserOrders : Screen("user_orders")
    object UserReservations : Screen("user_reservations")
    object UserNotifications : Screen("user_notifications")
    object UserAIChat : Screen("user_ai_chat")
    object UserReviewManagement : Screen("user_review_mgmt")
    object UserVouchers : Screen("user_vouchers")
    object UserSupport : Screen("user_support")
    object UserFoodDetail : Screen("user_food_detail/{foodId}") {
        fun createRoute(foodId: String) = "user_food_detail/$foodId"
    }
    object UserSubmitReview : Screen("user_submit_review/{orderId}") {
        fun createRoute(orderId: String) = "user_submit_review/$orderId"
    }

    // Admin Flow
    object AdminDashboard : Screen("admin_dashboard")
    object AdminReports : Screen("admin_reports")
    object AdminMenuManagement : Screen("admin_menu")
    object AdminAddEditFood : Screen("admin_add_edit_food/{foodId}") {
        fun createRoute(foodId: String) = "admin_add_edit_food/$foodId"
    }
    object AdminOrderManagement : Screen("admin_orders")
    object AdminBookingManagement : Screen("admin_booking")
    object AdminTableManagement : Screen("admin_tables")
    object AdminUserManagement : Screen("admin_users")
    object AdminVoucherManagement : Screen("admin_vouchers")
    object AdminAddEditVoucher : Screen("admin_add_edit_voucher/{voucherId}") {
        fun createRoute(voucherId: String) = "admin_add_edit_voucher/$voucherId"
    }
    object AdminReviews : Screen("admin_reviews")
    object AdminStatistics : Screen("admin_statistics")
    object AdminSupport : Screen("admin_support")
    object AdminChat : Screen("admin_chat/{roomId}") {
        fun createRoute(roomId: String) = "admin_chat/$roomId"
    }
    object AdminNotifications : Screen("admin_notifications")
    object AdminProfile : Screen("admin_profile")
}
