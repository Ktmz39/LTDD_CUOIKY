package com.example.app_doublekrestaurant.data.repository

import kotlin.Result

import com.example.app_doublekrestaurant.data.model.Voucher
import kotlinx.coroutines.flow.Flow

interface VoucherRepository {
    fun getVouchers(): Flow<List<Voucher>>
    suspend fun addVoucher(voucher: Voucher): Result<String>
    suspend fun updateVoucher(voucher: Voucher): Result<Unit>
    suspend fun deleteVoucher(voucherId: String): Result<Unit>
    suspend fun validateVoucher(code: String, orderAmount: Double): Result<Voucher>
}
