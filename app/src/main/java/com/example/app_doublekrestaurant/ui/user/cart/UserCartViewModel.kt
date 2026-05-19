package com.example.app_doublekrestaurant.ui.user.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.local.dao.CartDao
import com.example.app_doublekrestaurant.data.local.entity.toCartEntity
import com.example.app_doublekrestaurant.data.local.entity.toCartItem
import com.example.app_doublekrestaurant.data.model.*
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import com.example.app_doublekrestaurant.data.repository.NotificationRepository
import com.example.app_doublekrestaurant.data.repository.OrderRepository
import com.example.app_doublekrestaurant.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val selectedVoucher: Voucher? = null,
    val discountAmount: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class UserCartViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val voucherRepository: VoucherRepository,
    private val notificationRepository: NotificationRepository,
    private val cartDao: CartDao
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _claimedVouchers = MutableStateFlow<List<Voucher>>(emptyList())
    val claimedVouchers: StateFlow<List<Voucher>> = _claimedVouchers.asStateFlow()

    private var currentUser: User? = null

    init {
        // Lắng nghe thay đổi dữ liệu giỏ hàng từ Room Database thời gian thực
        viewModelScope.launch {
            cartDao.getCartItems().collect { entities ->
                _cartItems.value = entities.map { it.toCartItem() }
                recalculateCurrentDiscount()
            }
        }

        viewModelScope.launch {
            authRepository.currentUser.collect { user -> 
                currentUser = user 
                
                // Fetch details for claimed vouchers
                if (user != null && user.claimedVouchers.isNotEmpty()) {
                    launch {
                        voucherRepository.getVouchers().collect { allVouchers ->
                            _claimedVouchers.value = allVouchers.filter { 
                                it.id in user.claimedVouchers && it.isActive 
                            }
                        }
                    }
                } else {
                    _claimedVouchers.value = emptyList()
                }
            }
        }
    }

    fun addToCart(foodItem: FoodItem, quantity: Int = 1) {
        viewModelScope.launch {
            val existing = cartDao.getCartItemById(foodItem.id)
            if (existing != null) {
                cartDao.insertCartItem(existing.copy(quantity = existing.quantity + quantity))
            } else {
                val entity = CartItem(foodItem = foodItem, quantity = quantity).toCartEntity()
                cartDao.insertCartItem(entity)
            }
        }
    }

    fun removeFromCart(foodItemId: String) {
        viewModelScope.launch {
            cartDao.deleteCartItemById(foodItemId)
        }
    }

    fun updateQuantity(foodItemId: String, quantity: Int) {
        if (quantity <= 0) { 
            removeFromCart(foodItemId)
            return 
        }
        viewModelScope.launch {
            cartDao.updateQuantity(foodItemId, quantity)
        }
    }
    
    private fun recalculateCurrentDiscount() {
        val voucher = _uiState.value.selectedVoucher ?: return
        val newDiscount = calculateDiscount(voucher, getSubtotal())
        _uiState.value = _uiState.value.copy(discountAmount = newDiscount)
    }

    fun clearCart() { 
        viewModelScope.launch {
            cartDao.clearCart()
        }
        _uiState.value = _uiState.value.copy(selectedVoucher = null, discountAmount = 0.0)
    }
    
    fun applyVoucher(code: String) {
        if (code.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            voucherRepository.validateVoucher(code, getSubtotal()).fold(
                onSuccess = { voucher ->
                    val discount = calculateDiscount(voucher, getSubtotal())
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedVoucher = voucher,
                        discountAmount = discount,
                        error = "Áp dụng mã giảm giá thành công!"
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }
    
    fun removeVoucher() {
        _uiState.value = _uiState.value.copy(selectedVoucher = null, discountAmount = 0.0)
    }
    
    private fun calculateDiscount(voucher: Voucher, amount: Double): Double {
        return if (voucher.discountPercentage > 0) {
            val discount = amount * (voucher.discountPercentage / 100.0)
            if (voucher.maxDiscountAmount > 0) minOf(discount, voucher.maxDiscountAmount) else discount
        } else {
            minOf(voucher.discountAmount, amount)
        }
    }

    fun getSubtotal(): Double = _cartItems.value.sumOf { it.foodItem.price * it.quantity }
    fun getTotalPrice(): Double = maxOf(0.0, getSubtotal() - _uiState.value.discountAmount)
    fun getTotalCount(): Int = _cartItems.value.sumOf { it.quantity }

    fun checkout(orderType: OrderType = OrderType.DINE_IN, note: String = "", onSuccess: () -> Unit) {
        val user = currentUser ?: run {
            _uiState.value = _uiState.value.copy(error = "Vui lòng đăng nhập để đặt hàng")
            return
        }
        val items = _cartItems.value
        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Giỏ hàng trống")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingOut = true, error = null)

            val orderItems = items.map { cartItem ->
                OrderItem(
                    foodItemId = cartItem.foodItem.id,
                    name = cartItem.foodItem.name,
                    price = cartItem.foodItem.price,
                    quantity = cartItem.quantity,
                    imageUrl = cartItem.foodItem.imageUrl
                )
            }

            val order = Order(
                userId = user.uid,
                userName = user.fullName,
                userPhone = user.phone,
                userAvatarUrl = user.avatarUrl,
                items = orderItems,
                type = orderType.name,
                status = OrderStatus.PENDING.name,
                totalAmount = getTotalPrice(),
                note = note,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            orderRepository.placeOrder(order).fold(
                onSuccess = { orderId ->
                    val notification = Notification(
                        title = "Đặt hàng thành công!",
                        body = "Đơn hàng của bạn đang được nhà hàng chuẩn bị. Cảm ơn bạn!",
                        type = NotificationType.ORDER_UPDATE,
                        userId = user.uid
                    )
                    viewModelScope.launch {
                        notificationRepository.sendNotification(notification)
                    }
                    _uiState.value = _uiState.value.copy(isCheckingOut = false, checkoutSuccess = true)
                    clearCart()
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingOut = false,
                        error = "Đặt hàng thất bại: ${e.message}"
                    )
                }
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
