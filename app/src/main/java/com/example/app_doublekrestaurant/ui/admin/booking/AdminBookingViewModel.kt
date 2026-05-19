package com.example.app_doublekrestaurant.ui.admin.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Reservation
import com.example.app_doublekrestaurant.data.model.ReservationStatus
import com.example.app_doublekrestaurant.data.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBookingUiState(
    val isLoading: Boolean = true,
    val reservations: List<Reservation> = emptyList(),
    val filteredReservations: List<Reservation> = emptyList(),
    val selectedStatus: String = "ALL",
    val error: String? = null
)

@HiltViewModel
class AdminBookingViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminBookingUiState())
    val uiState: StateFlow<AdminBookingUiState> = _uiState.asStateFlow()

    init { loadReservations() }

    private fun loadReservations() {
        viewModelScope.launch {
            // userId = null means get ALL reservations (admin view)
            reservationRepository.getReservations(userId = null).collect { reservations ->
                val filtered = filterByStatus(reservations, _uiState.value.selectedStatus)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    reservations = reservations,
                    filteredReservations = filtered
                )
            }
        }
    }

    fun filterByStatus(statusFilter: String) {
        val filtered = filterByStatus(_uiState.value.reservations, statusFilter)
        _uiState.value = _uiState.value.copy(selectedStatus = statusFilter, filteredReservations = filtered)
    }

    private fun filterByStatus(reservations: List<Reservation>, status: String): List<Reservation> {
        return if (status == "ALL") reservations
        else reservations.filter { it.status == status }
    }

    fun updateStatus(reservationId: String, newStatus: ReservationStatus) {
        viewModelScope.launch {
            reservationRepository.updateReservationStatus(reservationId, newStatus).fold(
                onSuccess = { /* Refreshed via real-time listener */ },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(error = "Cập nhật thất bại: ${e.message}")
                }
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
