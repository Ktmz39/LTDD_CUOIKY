package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Reservation
import com.example.app_doublekrestaurant.data.model.ReservationStatus
import kotlinx.coroutines.flow.Flow
import kotlin.Result

interface ReservationRepository {
    fun getReservations(userId: String? = null): Flow<List<Reservation>>
    fun getReservationById(reservationId: String): Flow<Reservation?>
    suspend fun createReservation(reservation: Reservation): Result<String>
    suspend fun updateReservationStatus(reservationId: String, status: ReservationStatus): Result<Unit>
}
