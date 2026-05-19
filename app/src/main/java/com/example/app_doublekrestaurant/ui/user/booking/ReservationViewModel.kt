package com.example.app_doublekrestaurant.ui.user.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Reservation
import com.example.app_doublekrestaurant.data.model.RestaurantTable
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import com.example.app_doublekrestaurant.data.repository.ReservationRepository
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ReservationHistoryUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val restaurantRepository: RestaurantRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _availableTables = MutableStateFlow<List<RestaurantTable>>(emptyList())
    val availableTables: StateFlow<List<RestaurantTable>> = _availableTables.asStateFlow()

    private val _bookingStatus = MutableStateFlow<Result<String>?>(null)
    val bookingStatus: StateFlow<Result<String>?> = _bookingStatus.asStateFlow()

    private val _uiState = MutableStateFlow(ReservationHistoryUiState())
    val uiState: StateFlow<ReservationHistoryUiState> = _uiState.asStateFlow()

    init {
        fetchTables()
        loadUserReservations()
    }

    private fun fetchTables() {
        viewModelScope.launch {
            restaurantRepository.getTables().collect { tables ->
                _availableTables.value = tables.filter { it.isAvailable }
            }
        }
    }

    private fun loadUserReservations() {
        viewModelScope.launch {
            authRepository.currentUser.flatMapLatest { user ->
                if (user != null) {
                    reservationRepository.getReservations(userId = user.uid)
                } else {
                    flowOf(emptyList())
                }
            }.collect { reservations ->
                _uiState.value = _uiState.value.copy(
                    reservations = reservations.sortedByDescending { it.createdAt },
                    isLoading = false
                )
            }
        }
    }

    fun makeReservation(reservation: Reservation) {
        viewModelScope.launch {
            _bookingStatus.value = reservationRepository.createReservation(reservation)
        }
    }
}
