package com.example.app_doublekrestaurant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DoubleKApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to com.example.app_doublekrestaurant.util.CloudinaryConfig.CLOUD_NAME,
            "api_key" to com.example.app_doublekrestaurant.util.CloudinaryConfig.API_KEY,
            "api_secret" to com.example.app_doublekrestaurant.util.CloudinaryConfig.API_SECRET,
            "secure" to true
        )
        com.cloudinary.android.MediaManager.init(this, config)
    }
}
