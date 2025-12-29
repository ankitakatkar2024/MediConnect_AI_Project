package com.example.mediconnect_ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationHistoryDao {
    @Insert
    suspend fun insert(notification: NotificationHistory)

    @Query("SELECT * FROM notification_history_table ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): List<NotificationHistory>
}