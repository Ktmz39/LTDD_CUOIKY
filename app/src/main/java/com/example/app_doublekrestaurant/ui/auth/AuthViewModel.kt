package com.example.app_doublekrestaurant.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.model.UserRole
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false,
    val role: UserRole? = null,
    val isUploadingImage: Boolean = false,
    val uploadProgress: Float = 0f
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state changes
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoggedIn = user != null,
                    role = user?.role
                )
            }
        }
    }

    fun login(email: String, password: String, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(isLoading = false, currentUser = user)
                    onSuccess(user.role)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e.message)
                    )
                }
            )
        }
    }

    fun register(fullName: String, email: String, phone: String, password: String, role: UserRole = UserRole.USER, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val newUser = User(fullName = fullName, email = email, phone = phone, role = role)
            val result = authRepository.register(newUser, password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = mapFirebaseError(e.message)
                    )
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
            onComplete()
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.resetPassword(email)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = if (result.isFailure) "Không thể gửi email đặt lại mật khẩu" else null
            )
        }
    }

    fun updateAvatarUrl(url: String) {
        val uid = _uiState.value.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.updateAvatarUrl(uid, url)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = "Không thể cập nhật ảnh đại diện")
            }
        }
    }

    fun uploadAvatar(uri: android.net.Uri) {
        _uiState.value = _uiState.value.copy(isUploadingImage = true, uploadProgress = 0f)
        com.example.app_doublekrestaurant.util.CloudinaryService.uploadImage(
            uri = uri,
            onSuccess = { url ->
                _uiState.value = _uiState.value.copy(isUploadingImage = false)
                updateAvatarUrl(url)
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(isUploadingImage = false, error = error)
            },
            onProgress = { progress ->
                _uiState.value = _uiState.value.copy(uploadProgress = progress.toFloat())
            }
        )
    }

    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.changePassword(oldPassword, newPassword)
            _uiState.value = _uiState.value.copy(isLoading = false)
            result.fold(
                onSuccess = {
                    onSuccess()
                },
                onFailure = { e ->
                    val errorMsg = when {
                        e.message?.contains("wrong-password", ignoreCase = true) == true || e.message?.contains("invalid-credential", ignoreCase = true) == true -> "Mật khẩu hiện tại không chính xác"
                        e.message?.contains("weak-password", ignoreCase = true) == true -> "Mật khẩu mới quá yếu (tối thiểu 6 ký tự)"
                        else -> mapFirebaseError(e.message)
                    }
                    _uiState.value = _uiState.value.copy(error = errorMsg)
                    onFailure(errorMsg)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun mapFirebaseError(message: String?): String {
        return when {
            message == null -> "Đã có lỗi xảy ra"
            message.contains("INVALID_LOGIN_CREDENTIALS") || message.contains("invalid-credential") -> "Email hoặc mật khẩu không đúng"
            message.contains("EMAIL_EXISTS") || message.contains("email-already-in-use") -> "Email này đã được sử dụng"
            message.contains("WEAK_PASSWORD") || message.contains("weak-password") -> "Mật khẩu quá yếu (tối thiểu 6 ký tự)"
            message.contains("INVALID_EMAIL") || message.contains("invalid-email") -> "Email không hợp lệ"
            message.contains("network") || message.contains("NETWORK") -> "Lỗi kết nối mạng"
            else -> "Đăng nhập thất bại. Vui lòng thử lại."
        }
    }
}
