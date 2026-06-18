package com.example.app_doublekrestaurant.ui.user.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("0", "Xin chào! Tôi là trợ lý AI của DoubleK Restaurant. Tôi có thể giúp gì cho bạn?", false)
    ),
    val isTyping: Boolean = false
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Thêm tin nhắn của người dùng vào giao diện và bật hiệu ứng đang tải (isTyping = true)
        val userMsg = ChatMessage(System.currentTimeMillis().toString(), text, true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            isTyping = true
        )

        // 2. Gọi API Gemini trong luồng Coroutine ngầm
        viewModelScope.launch {
            val responseText = try {
                // Gửi tin nhắn lên Gemini Server
                val response = generativeModel.generateContent(text)
                response.text ?: "Xin lỗi, tôi gặp chút trục trặc khi xử lý câu hỏi này."
            } catch (e: Exception) {
                e.printStackTrace()
                "Không thể kết nối Internet hoặc lỗi hệ thống AI. Vui lòng thử lại sau nhé!"
            }

            // 3. Nhận kết quả phản hồi từ AI, thêm vào giao diện và tắt hiệu ứng đang tải (isTyping = false)
            val aiMsg = ChatMessage((System.currentTimeMillis() + 1).toString(), responseText, false)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMsg,
                isTyping = false
            )
        }
    }
}