package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.Reservation
import com.example.app_doublekrestaurant.data.model.ReservationStatus
import com.example.app_doublekrestaurant.data.model.TableStatus
import com.example.app_doublekrestaurant.data.model.NotificationType
import com.example.app_doublekrestaurant.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Result

class ReservationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) : ReservationRepository {

    override fun getReservationById(reservationId: String): Flow<Reservation?> = callbackFlow {
        val subscription = firestore.collection("reservations").document(reservationId)
            .addSnapshotListener { snapshot, _ ->
                val reservation = snapshot?.toObject(Reservation::class.java)?.copy(id = snapshot.id)
                trySend(reservation)
            }
        awaitClose { subscription.remove() }
    }

    override fun getReservations(userId: String?): Flow<List<Reservation>> = callbackFlow {
        val query: Query = if (userId != null) {
            firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
        } else {
            firestore.collection("reservations")
                .orderBy("createdAt", Query.Direction.DESCENDING)
        }
        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) { trySend(emptyList()); return@addSnapshotListener }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Reservation::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun createReservation(reservation: Reservation): Result<String> = try {
        val docRef = firestore.collection("reservations").document()
        val finalReservation = reservation.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(finalReservation).await()

        // Mark the table as reserved
        if (reservation.tableId.isNotEmpty()) {
            firestore.collection("tables").document(reservation.tableId)
                .update("status", TableStatus.RESERVED.name)
                .await()
        }

        // Send notification to Admin
        notificationRepository.sendNotification(
            Notification(
                title = "Yêu cầu đặt bàn mới 🍽️",
                body = "Khách hàng ${reservation.userName} vừa đặt bàn ${reservation.tableNumber} cho ${reservation.guestCount} khách vào ${reservation.date} lúc ${reservation.time}",
                userId = "", // Broadcast to admin
                type = NotificationType.RESERVATION_UPDATE,
                referenceId = docRef.id
            )
        )

        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateReservationStatus(
        reservationId: String,
        status: ReservationStatus
    ): Result<Unit> = try {
        firestore.collection("reservations").document(reservationId)
            .update("status", status.name)
            .await()

        // Send notification to User
        val reservation = getReservationById(reservationId).firstOrNull()
        if (reservation != null) {
            // Cập nhật trạng thái bàn ăn thực tế trong collection "tables"
            if (reservation.tableId.isNotEmpty()) {
                val tableStatus = when (status) {
                    ReservationStatus.CONFIRMED -> TableStatus.RESERVED.name
                    ReservationStatus.SEATED -> TableStatus.OCCUPIED.name
                    ReservationStatus.COMPLETED, ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW -> TableStatus.AVAILABLE.name
                    else -> null
                }
                if (tableStatus != null) {
                    firestore.collection("tables").document(reservation.tableId)
                        .update("status", tableStatus)
                        .await()
                }
            }

            val statusMsg = when (status) {
                ReservationStatus.CONFIRMED -> "Yêu cầu đặt bàn của bạn đã được xác nhận"
                ReservationStatus.SEATED -> "Chào mừng bạn đến với nhà hàng! Chúc bạn ngon miệng"
                ReservationStatus.COMPLETED -> "Cảm ơn bạn đã đến với chúng tôi. Hẹn gặp lại!"
                ReservationStatus.CANCELLED -> "Rất tiếc, yêu cầu đặt bàn của bạn đã bị hủy"
                else -> ""
            }
            if (statusMsg.isNotEmpty()) {
                notificationRepository.sendNotification(
                    Notification(
                        title = "Cập nhật đặt bàn",
                        body = statusMsg,
                        userId = reservation.userId,
                        type = NotificationType.RESERVATION_UPDATE,
                        referenceId = reservationId
                    )
                )
            }
        }

        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun cancelReservation(reservationId: String): Result<Unit> =
        updateReservationStatus(reservationId, ReservationStatus.CANCELLED)
}
