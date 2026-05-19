package com.example.app_doublekrestaurant.ui.admin.vouchers

import kotlin.Result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Notification
import com.example.app_doublekrestaurant.data.model.NotificationType
import com.example.app_doublekrestaurant.data.model.Voucher
import com.example.app_doublekrestaurant.data.repository.NotificationRepository
import com.example.app_doublekrestaurant.data.repository.VoucherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoucherManagementUiState(
    val isLoading: Boolean = false,
    val vouchers: List<Voucher> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AdminVoucherViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherManagementUiState(isLoading = true))
    val uiState: StateFlow<VoucherManagementUiState> = _uiState.asStateFlow()

    init {
        loadVouchers()
    }

    private fun loadVouchers() {
        viewModelScope.launch {
            voucherRepository.getVouchers().collect { vouchers ->
                _uiState.value = _uiState.value.copy(isLoading = false, vouchers = vouchers)
            }
        }
    }

    fun addOrUpdateVoucher(voucher: Voucher) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val isNew = voucher.id.isEmpty()
            val result = if (isNew) {
                voucherRepository.addVoucher(voucher)
            } else {
                voucherRepository.updateVoucher(voucher)
            }
            
            result.fold(
                onSuccess = {
                    if (isNew) {
                        val notification = Notification(
                            title = "Voucher mới: ${voucher.code}",
                            body = voucher.description,
                            type = NotificationType.PROMOTION,
                            userId = "" // broadcast to all
                        )
                        viewModelScope.launch {
                            notificationRepository.sendNotification(notification)
                        }
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true) 
                },
                onFailure = { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            )
        }
    }

    fun deleteVoucher(id: String) {
        viewModelScope.launch {
            voucherRepository.deleteVoucher(id)
        }
    }

    fun toggleVoucher(voucher: Voucher) {
        addOrUpdateVoucher(voucher.copy(isActive = !voucher.isActive))
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, isSuccess = false)
    }
}
