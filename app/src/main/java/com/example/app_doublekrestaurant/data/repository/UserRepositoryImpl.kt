package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.Result

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUsers(): Flow<List<User>> = callbackFlow {
        val subscription = firestore.collection("users")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.copy(uid = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateUserRole(userId: String, role: String): Result<Unit> = try {
        firestore.collection("users").document(userId).update("role", role).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun toggleUserActive(userId: String, isActive: Boolean): Result<Unit> = try {
        firestore.collection("users").document(userId).update("isActive", isActive).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteUser(userId: String): Result<Unit> = try {
        firestore.collection("users").document(userId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun claimVoucher(userId: String, voucherId: String): Result<Unit> = try {
        firestore.collection("users").document(userId)
            .update("claimedVouchers", com.google.firebase.firestore.FieldValue.arrayUnion(voucherId))
            .await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
