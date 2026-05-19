package com.example.app_doublekrestaurant.ui.user.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_doublekrestaurant.data.model.Reservation
import com.example.app_doublekrestaurant.data.model.ReservationStatus
import com.example.app_doublekrestaurant.data.model.RestaurantTable
import com.example.app_doublekrestaurant.data.model.TableStatus
import com.example.app_doublekrestaurant.data.repository.AuthRepository
import com.example.app_doublekrestaurant.data.repository.ReservationRepository
import com.example.app_doublekrestaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = false,
    val tables: List<RestaurantTable> = emptyList(),
    val reservations: List<Reservation> = emptyList(),
    val selectedTableId: String? = null,
    val guestCount: Int = 2,
    val date: String = "",
    val time: String = "",
    val note: String = "",
    val selectedAmenities: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val reservationRepository: ReservationRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: com.example.app_doublekrestaurant.data.repository.NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState(isLoading = true))
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private var currentUser: com.example.app_doublekrestaurant.data.model.User? = null

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user -> currentUser = user }
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load tables
            launch {
                restaurantRepository.getTables().collect { tables ->
                    _uiState.value = _uiState.value.copy(tables = tables)
                    checkLoadingComplete()
                }
            }
            // Load current user's reservations to prevent duplicate booking
            launch {
                authRepository.currentUser.collect { user ->
                    if (user != null) {
                        reservationRepository.getReservations(userId = user.uid).collect { reservations ->
                            _uiState.value = _uiState.value.copy(reservations = reservations)
                            checkLoadingComplete()
                        }
                    }
                }
            }
        }
    }

    private fun checkLoadingComplete() {
        if (_uiState.value.isLoading) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun isTableAvailableForBooking(table: RestaurantTable, date: String, time: String): Boolean {
        if (!table.isAvailable) return false // Đã bị đổi thành RESERVED, OCCUPIED, hoặc UNAVAILABLE
        if (date.isEmpty() || time.isEmpty()) return true // Mặc định hiển thị khả dụng nếu chưa nhập Ngày/Giờ
        return !_uiState.value.reservations.any { res ->
            res.tableId == table.id &&
            res.date == date &&
            res.time == time &&
            res.status != ReservationStatus.CANCELLED.name
        }
    }

    fun selectTable(tableId: String) {
        _uiState.value = _uiState.value.copy(selectedTableId = tableId)
    }

    fun updateGuestCount(count: Int) {
        if (count in 1..30) _uiState.value = _uiState.value.copy(guestCount = count)
    }

    fun updateDate(date: String) {
        val selectedTable = _uiState.value.tables.firstOrNull { it.id == _uiState.value.selectedTableId }
        val newSelectedTableId = if (selectedTable != null && !isTableAvailableForBooking(selectedTable, date, _uiState.value.time)) {
            null
        } else {
            _uiState.value.selectedTableId
        }
        _uiState.value = _uiState.value.copy(date = date, selectedTableId = newSelectedTableId)
    }

    fun updateTime(time: String) {
        val selectedTable = _uiState.value.tables.firstOrNull { it.id == _uiState.value.selectedTableId }
        val newSelectedTableId = if (selectedTable != null && !isTableAvailableForBooking(selectedTable, _uiState.value.date, time)) {
            null
        } else {
            _uiState.value.selectedTableId
        }
        _uiState.value = _uiState.value.copy(time = time, selectedTableId = newSelectedTableId)
    }

    fun updateNote(note: String) { _uiState.value = _uiState.value.copy(note = note) }

    fun toggleAmenity(amenity: String) {
        val current = _uiState.value.selectedAmenities.toMutableList()
        if (current.contains(amenity)) current.remove(amenity) else current.add(amenity)
        _uiState.value = _uiState.value.copy(selectedAmenities = current)
    }

    fun submitBooking(onSuccess: () -> Unit) {
        val state = _uiState.value
        val user = currentUser ?: run {
            _uiState.value = state.copy(error = "Vui lòng đăng nhập để đặt bàn")
            return
        }
        val table = state.tables.firstOrNull { it.id == state.selectedTableId } ?: run {
            _uiState.value = state.copy(error = "Vui lòng chọn bàn")
            return
        }

        // Kiểm tra xem bàn đã được đặt ở thời gian này chưa
        if (!isTableAvailableForBooking(table, state.date, state.time)) {
            _uiState.value = state.copy(error = "Rất tiếc! Bàn ${table.number} đã có người đặt vào lúc ${state.time} ngày ${state.date}. Vui lòng chọn bàn khác.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)

            val amenityNote = if (state.selectedAmenities.isNotEmpty()) "\nTiện ích: ${state.selectedAmenities.joinToString(", ")}" else ""
            val reservation = Reservation(
                userId = user.uid,
                userName = user.fullName,
                userPhone = user.phone,
                userEmail = user.email,
                date = state.date,
                time = state.time,
                guestCount = state.guestCount,
                tableId = table.id,
                tableNumber = table.number,
                note = state.note + amenityNote,
                status = ReservationStatus.PENDING.name
            )

            reservationRepository.createReservation(reservation).fold(
                onSuccess = { resId ->
                    val notification = com.example.app_doublekrestaurant.data.model.Notification(
                        title = "Đặt bàn thành công!",
                        body = "Bàn ${table.number} đã được đặt cho ${state.guestCount} người vào lúc ${state.time} ngày ${state.date}.",
                        type = com.example.app_doublekrestaurant.data.model.NotificationType.RESERVATION_UPDATE,
                        userId = user.uid
                    )
                    viewModelScope.launch {
                        notificationRepository.sendNotification(notification)
                    }
                    _uiState.value = _uiState.value.copy(isSubmitting = false, isSuccess = true)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message ?: "Đặt bàn thất bại")
                }
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
