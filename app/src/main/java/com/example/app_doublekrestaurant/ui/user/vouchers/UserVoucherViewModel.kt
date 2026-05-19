package com.example.app_doublekrestaurant.ui.user.vouchers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.model.Voucher
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import com.example.app_doublekrestaurant.data.repository.UserRepository
import com.example.app_doublekrestaurant.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserVoucherUiState(
    val isLoading: Boolean = false,
    val vouchers: List<Voucher> = emptyList(),
    val currentUser: User? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class UserVoucherViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserVoucherUiState(isLoading = true))
    val uiState: StateFlow<UserVoucherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Collect current user
            launch {
                authRepository.currentUser.collect { user ->
                    _uiState.value = _uiState.value.copy(currentUser = user)
                }
            }
            
            // Collect all active vouchers
            launch {
                voucherRepository.getVouchers().collect { allVouchers ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        vouchers = allVouchers.filter { it.isActive }
                    )
                }
            }
        }
    }

    fun claimVoucher(voucherId: String) {
        val user = _uiState.value.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = "Vui lòng đăng nhập để nhận mã")
            return
        }

        viewModelScope.launch {
            userRepository.claimVoucher(user.uid, voucherId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Có lỗi xảy ra")
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, isSuccess = false)
    }
}
