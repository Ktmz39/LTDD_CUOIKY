package com.example.app_doublekrestaurant.data.local.dao

import androidx.room.*
import com.example.app_doublekrestaurant.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_items WHERE foodItemId = :foodItemId LIMIT 1")
    suspend fun getCartItemById(foodItemId: String): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartEntity: CartEntity)

    @Query("DELETE FROM cart_items WHERE foodItemId = :foodItemId")
    suspend fun deleteCartItemById(foodItemId: String)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE foodItemId = :foodItemId")
    suspend fun updateQuantity(foodItemId: String, quantity: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
