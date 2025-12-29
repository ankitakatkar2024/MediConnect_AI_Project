package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history_table")
data class NotificationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)