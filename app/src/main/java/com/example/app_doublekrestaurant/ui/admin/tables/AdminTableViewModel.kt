package com.example.app_doublekrestaurant.ui.admin.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.RestaurantTable
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TableManagementUiState(
    val isLoading: Boolean = false,
    val tables: List<RestaurantTable> = emptyList(),
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AdminTableViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableManagementUiState(isLoading = true))
    val uiState: StateFlow<TableManagementUiState> = _uiState.asStateFlow()

    init {
        loadTables()
    }

    private fun loadTables() {
        viewModelScope.launch {
            restaurantRepository.getTables().collect { tables ->
                _uiState.value = _uiState.value.copy(isLoading = false, tables = tables)
            }
        }
    }

    fun addOrUpdateTable(table: RestaurantTable) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = if (table.id.isEmpty()) {
                restaurantRepository.addTable(table)
            } else {
                restaurantRepository.updateTable(table)
            }
            
            result.fold(
                onSuccess = { 
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    loadTables() // Reload to get the new table
                },
                onFailure = { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
            )
        }
    }

    fun deleteTable(id: String) {
        viewModelScope.launch {
            restaurantRepository.deleteTable(id)
        }
    }

    fun toggleAvailability(table: RestaurantTable) {
        val newStatus = if (table.isAvailable) "OCCUPIED" else "AVAILABLE"
        addOrUpdateTable(table.copy(status = newStatus))
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, isSuccess = false)
    }
}
