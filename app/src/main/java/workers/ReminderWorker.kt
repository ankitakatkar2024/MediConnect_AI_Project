package com.example.mediconnect_ai.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mediconnect_ai.DailyTasksActivity
import com.example.mediconnect_ai.R
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.NotificationHistory
import java.util.Calendar

class ReminderWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // MODIFIED: Use the taskDao
        val taskDao = AppDatabase.getInstance(applicationContext).taskDao()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfToday = calendar.timeInMillis

        // MODIFIED: Get the count of today's incomplete tasks
        val taskCount = taskDao.getIncompleteTasksForTodayCount(startOfToday, endOfToday)

        if (taskCount > 0) {
            showNotification(taskCount)
        }

        return Result.success()
    }

    private suspend fun showNotification(taskCount: Int) {
        val intent = Intent(applicationContext, DailyTasksActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val title = "MediConnect AI Daily Tasks"
        // MODIFIED: The notification text now refers to "tasks"
        val notificationText = if (taskCount == 1) "You have 1 pending task today." else "You have $taskCount pending tasks today."

        val builder = NotificationCompat.Builder(applicationContext, "MEDICONNECT_REMINDERS")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a built-in icon
            .setContentTitle(title)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationHistoryDao = AppDatabase.getInstance(applicationContext).notificationHistoryDao()
        val notificationRecord = NotificationHistory(title = title, message = notificationText)
        notificationHistoryDao.insert(notificationRecord)

        NotificationManagerCompat.from(applicationContext).notify(1, builder.build())
    }
}