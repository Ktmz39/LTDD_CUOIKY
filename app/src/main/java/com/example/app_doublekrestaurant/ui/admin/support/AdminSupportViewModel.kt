package com.example.app_doublekrestaurant.ui.admin.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.SupportRoom
import com.example.app_doublekrestaurant.data.repository.SupportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminSupportUiState(
    val chatRooms: List<SupportRoom> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AdminSupportViewModel @Inject constructor(
    private val supportRepository: SupportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSupportUiState())
    val uiState: StateFlow<AdminSupportUiState> = _uiState.asStateFlow()

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        viewModelScope.launch {
            supportRepository.getChatRooms()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
                .collect { rooms ->
                    _uiState.value = _uiState.value.copy(
                        chatRooms = rooms,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
}
