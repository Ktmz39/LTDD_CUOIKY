package com.example.app_doublekrestaurant.data.repository

import kotlin.Result

import com.example.app_doublekrestaurant.data.model.Voucher
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VoucherRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VoucherRepository {

    override fun getVouchers(): Flow<List<Voucher>> = callbackFlow {
        val subscription = firestore.collection("vouchers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Voucher(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            type = doc.getString("type") ?: "PERCENTAGE",
                            iconType = doc.getString("iconType") ?: "TICKET",
                            startDate = doc.getLong("startDate") ?: 0L,
                            code = doc.getString("code") ?: "",
                            description = doc.getString("description") ?: "",
                            discountAmount = doc.getDouble("discountAmount") ?: doc.getLong("discountAmount")?.toDouble() ?: 0.0,
                            discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0,
                            minOrderAmount = doc.getDouble("minOrderAmount") ?: doc.getLong("minOrderAmount")?.toDouble() ?: 0.0,
                            maxDiscountAmount = doc.getDouble("maxDiscountAmount") ?: doc.getLong("maxDiscountAmount")?.toDouble() ?: 0.0,
                            expiryDate = doc.getLong("expiryDate") ?: 0L,
                            isActive = doc.getBoolean("isActive") ?: true,
                            usageLimit = doc.getLong("usageLimit")?.toInt() ?: 0,
                            usedCount = doc.getLong("usedCount")?.toInt() ?: 0,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(list.sortedByDescending { it.createdAt })
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addVoucher(voucher: Voucher): Result<String> = try {
        val docRef = firestore.collection("vouchers").document()
        val finalVoucher = voucher.copy(id = docRef.id, createdAt = System.currentTimeMillis())
        docRef.set(finalVoucher).await()
        Result.success(docRef.id)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateVoucher(voucher: Voucher): Result<Unit> = try {
        firestore.collection("vouchers").document(voucher.id).set(voucher).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteVoucher(voucherId: String): Result<Unit> = try {
        firestore.collection("vouchers").document(voucherId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun validateVoucher(code: String, orderAmount: Double): Result<Voucher> = try {
        val cleanCode = code.trim()
        val snapshot = firestore.collection("vouchers").get().await()
            
        val doc = snapshot.documents.firstOrNull { 
            val codeMatches = it.getString("code")?.trim().equals(cleanCode, ignoreCase = true)
            val isActive = it.getBoolean("isActive") ?: true
            codeMatches && isActive
        } ?: throw Exception("Mã giảm giá không tồn tại hoặc đã hết hạn")
        val voucher = Voucher(
            id = doc.id,
            title = doc.getString("title") ?: "",
            type = doc.getString("type") ?: "PERCENTAGE",
            iconType = doc.getString("iconType") ?: "TICKET",
            startDate = doc.getLong("startDate") ?: 0L,
            code = doc.getString("code") ?: "",
            description = doc.getString("description") ?: "",
            discountAmount = doc.getDouble("discountAmount") ?: doc.getLong("discountAmount")?.toDouble() ?: 0.0,
            discountPercentage = doc.getLong("discountPercentage")?.toInt() ?: 0,
            minOrderAmount = doc.getDouble("minOrderAmount") ?: doc.getLong("minOrderAmount")?.toDouble() ?: 0.0,
            maxDiscountAmount = doc.getDouble("maxDiscountAmount") ?: doc.getLong("maxDiscountAmount")?.toDouble() ?: 0.0,
            expiryDate = doc.getLong("expiryDate") ?: 0L,
            isActive = doc.getBoolean("isActive") ?: true,
            usageLimit = doc.getLong("usageLimit")?.toInt() ?: 0,
            usedCount = doc.getLong("usedCount")?.toInt() ?: 0,
            createdAt = doc.getLong("createdAt") ?: 0L
        )
            
        if (voucher.expiryDate > 0 && voucher.expiryDate < System.currentTimeMillis()) {
            throw Exception("Mã giảm giá đã hết hạn")
        }
        
        if (voucher.usageLimit > 0 && voucher.usedCount >= voucher.usageLimit) {
            throw Exception("Mã giảm giá đã hết lượt sử dụng")
        }
        
        if (orderAmount < voucher.minOrderAmount) {
            throw Exception("Đơn hàng tối thiểu để sử dụng mã này là ${voucher.minOrderAmount}")
        }
        
        Result.success(voucher)
    } catch (e: Exception) { Result.failure(e) }
}
