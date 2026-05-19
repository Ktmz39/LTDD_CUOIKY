package com.example.app_doublekrestaurant.ui.user.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
class AIChatViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(System.currentTimeMillis().toString(), text, true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            isTyping = true
        )

        viewModelScope.launch {
            delay(1500) // Simulating AI thinking
            val response = getAIResponse(text)
            val aiMsg = ChatMessage((System.currentTimeMillis() + 1).toString(), response, false)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMsg,
                isTyping = false
            )
        }
    }

    private fun getAIResponse(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("thực đơn") || lower.contains("món ăn") -> "Chúng tôi có thực đơn đa dạng với các món đặc trưng như Bò né DoubleK, Lẩu hải sản và nhiều món ăn kèm hấp dẫn khác. Bạn có thể xem chi tiết trong phần 'Thực đơn' nhé!"
            lower.contains("đặt bàn") -> "Để đặt bàn, bạn vui lòng vào mục 'Đặt bàn' ở trang chủ, chọn thời gian và bàn bạn thích. Admin sẽ xác nhận yêu cầu của bạn nhanh nhất có thể!"
            lower.contains("giờ mở cửa") -> "DoubleK Restaurant mở cửa từ 09:00 đến 22:00 hàng ngày, kể cả cuối tuần và ngày lễ."
            lower.contains("vị trí") || lower.contains("địa chỉ") -> "Chúng tôi tọa lạc tại trung tâm thành phố. Bạn có thể tìm thấy định vị chính xác trong mục 'Hỗ trợ'."
            lower.contains("khuyến mãi") || lower.contains("voucher") -> "Hiện tại chúng tôi đang có chương trình giảm 50k cho đơn hàng đầu tiên. Hãy kiểm tra mục 'Voucher' để xem thêm các ưu đãi khác nhé!"
            else -> "Cảm ơn bạn đã quan tâm! Tôi có thể hỗ trợ bạn về thực đơn, đặt bàn, hoặc các chương trình khuyến mãi hiện có của nhà hàng."
        }
    }
}
