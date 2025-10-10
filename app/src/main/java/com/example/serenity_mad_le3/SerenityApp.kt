package com.example.serenity_mad_le3

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SerenityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Register the notification channel once so reminders can fire on Android O+.
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water"
            }
            val manager = getSystemService(NotificationManager::class.java)
            // Safe to call multiple times; the system ignores duplicate definitions.
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "hydration"
    }
}
