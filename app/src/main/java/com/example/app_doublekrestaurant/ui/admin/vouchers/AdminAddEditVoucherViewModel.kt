package com.example.app_doublekrestaurant.ui.admin.vouchers

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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditVoucherUiState(
    val isLoading: Boolean = false,
    val voucher: Voucher? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AdminAddEditVoucherViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditVoucherUiState())
    val uiState: StateFlow<AddEditVoucherUiState> = _uiState.asStateFlow()

    fun loadVoucher(id: String?) {
        if (id == null) return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val list = voucherRepository.getVouchers().firstOrNull()
            val voucher = list?.find { it.id == id }
            _uiState.value = _uiState.value.copy(isLoading = false, voucher = voucher)
        }
    }

    fun saveVoucher(voucher: Voucher) {
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
                            title = "Ưu đãi mới: ${voucher.title.ifEmpty { voucher.code }}",
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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, isSuccess = false)
    }
}
