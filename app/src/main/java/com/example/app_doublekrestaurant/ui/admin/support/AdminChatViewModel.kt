package com.example.app_doublekrestaurant.ui.admin.support

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.SupportMessage
import com.example.app_doublekrestaurant.data.repository.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminChatUiState(
    val messages: List<SupportMessage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AdminChatViewModel @Inject constructor(
    private val supportRepository: SupportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = checkNotNull(savedStateHandle["roomId"])

    private val _uiState = MutableStateFlow(AdminChatUiState())
    val uiState: StateFlow<AdminChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        markMessagesAsRead()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            supportRepository.getMessages(roomId)
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
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                supportRepository.sendMessage(roomId, content, isAdmin = true)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun markMessagesAsRead() {
        viewModelScope.launch {
            try {
                supportRepository.markMessagesAsRead(roomId, isAdmin = true)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
