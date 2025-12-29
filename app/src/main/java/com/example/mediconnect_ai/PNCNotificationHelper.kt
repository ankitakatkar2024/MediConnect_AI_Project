package com.example.mediconnect_ai.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mediconnect_ai.PNCScheduleActivity
import com.example.mediconnect_ai.R

object PNCNotificationHelper {

    private const val CHANNEL_ID = "pnc_reminders"
    private const val CHANNEL_NAME = "PNC Visit Reminders"

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for post-natal care visits"
                    enableLights(true)
                    lightColor = Color.MAGENTA
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun showPNCReminder(
        context: Context,
        patientId: Long,
        patientName: String,
        visitName: String
    ) {
        ensureChannel(context)

        // Open PNC schedule when user taps the notification
        val intent = Intent(context, PNCScheduleActivity::class.java).apply {
            putExtra(PNCScheduleActivity.EXTRA_PATIENT_ID, patientId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            patientId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning) // or any small icon you already have
            .setContentTitle("PNC visit today: $patientName")
            .setContentText(visitName)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$visitName for $patientName is due today.")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(
            (patientId * 100 + System.currentTimeMillis() % 100).toInt(),
            notification
        )
    }
}
