package com.example.app_doublekrestaurant.ui.admin.users

import kotlin.Result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserManagementUiState(isLoading = true))
    val uiState: StateFlow<UserManagementUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            userRepository.getUsers().collect { users ->
                _uiState.value = _uiState.value.copy(isLoading = false, users = users)
            }
        }
    }

    fun updateUserRole(userId: String, role: String) {
        viewModelScope.launch {
            userRepository.updateUserRole(userId, role)
        }
    }

    fun toggleUserActive(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            userRepository.toggleUserActive(userId, isActive)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
        }
    }
}
