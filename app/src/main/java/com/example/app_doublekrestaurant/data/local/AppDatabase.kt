package com.example.app_doublekrestaurant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.app_doublekrestaurant.data.local.dao.CartDao
import com.example.app_doublekrestaurant.data.local.entity.CartEntity

@Database(entities = [CartEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val cartDao: CartDao
}
