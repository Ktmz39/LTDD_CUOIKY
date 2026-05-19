package com.example.app_doublekrestaurant.di

import android.content.Context
import androidx.room.Room
import com.example.app_doublekrestaurant.data.local.AppDatabase
import com.example.app_doublekrestaurant.data.local.dao.CartDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "doublek_restaurant.db"
    ).fallbackToDestructiveMigration() // Thích hợp cho môi trường phát triển nếu schema thay đổi
        .build()

    @Provides
    @Singleton
    fun provideCartDao(database: AppDatabase): CartDao = database.cartDao
}
