package com.example.app_doublekrestaurant.ui.user.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.SupportMessage
import com.example.app_doublekrestaurant.data.repository.SupportRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserSupportUiState(
    val messages: List<SupportMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class UserSupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSupportUiState())
    val uiState: StateFlow<UserSupportUiState> = _uiState.asStateFlow()

    private val userId = auth.currentUser?.uid

    init {
        if (userId != null) {
            loadMessages(userId)
            markMessagesAsRead(userId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in")
        }
    }

    private fun loadMessages(uid: String) {
        viewModelScope.launch {
            supportRepository.getMessages(uid)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || userId == null) return
        
        viewModelScope.launch {
            try {
                supportRepository.sendMessage(userId, content, isAdmin = false)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun markMessagesAsRead(uid: String) {
        viewModelScope.launch {
            try {
                supportRepository.markMessagesAsRead(uid, isAdmin = false)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
