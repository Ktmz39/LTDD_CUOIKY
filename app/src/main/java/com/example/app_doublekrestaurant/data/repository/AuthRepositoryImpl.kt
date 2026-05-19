package com.example.app_doublekrestaurant.data.repository

import com.example.app_doublekrestaurant.data.model.User
import com.example.app_doublekrestaurant.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                // Real-time listener on the user's Firestore document
                firestore.collection("users").document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null || snapshot == null) return@addSnapshotListener
                        val user = snapshot.toObject(User::class.java)?.copy(uid = snapshot.id)
                        trySend(user)
                    }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: throw Exception("Login failed")
        val snapshot = firestore.collection("users").document(uid).get().await()
        val user = snapshot.toObject(User::class.java)?.copy(uid = uid)
            ?: throw Exception("Không tìm thấy tài khoản trong hệ thống")
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun register(user: User, password: String): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(user.email, password).await()
        val uid = result.user?.uid ?: throw Exception("Registration failed")
        val finalUser = user.copy(uid = uid, createdAt = System.currentTimeMillis())
        firestore.collection("users").document(uid).set(finalUser).await()
        Result.success(finalUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateAvatarUrl(uid: String, url: String): Result<Unit> = try {
        firestore.collection("users").document(uid).update("avatarUrl", url).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("Người dùng chưa đăng nhập")
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, oldPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
