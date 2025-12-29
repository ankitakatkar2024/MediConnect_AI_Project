package com.example.mediconnect_ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mediconnect_ai.workers.ReminderWorker
import java.util.concurrent.TimeUnit

class MediConnectApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // From your recent code: Apply the saved language at startup
        LocaleManager.applyPersistedLocale(this)

        // From the update: Create the notification channel for reminders
        createNotificationChannel()

        // From the update: Schedule the daily background reminder task
        scheduleDailyReminder()
    }

    /**
     * Creates a Notification Channel, required for Android 8 (Oreo) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MediConnect Reminders"
            val descriptionText = "Notifications for daily tasks and appointments"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MEDICONNECT_REMINDERS", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules a background task using WorkManager to run once every 24 hours.
     */
    private fun scheduleDailyReminder() {
        // Define the repeating work request
        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(2, TimeUnit.MINUTES)
            .build()

        // Enqueue the work with a unique name to prevent it from being scheduled multiple times
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyReminder",
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing work if it's already scheduled
            reminderRequest
        )
    }
}