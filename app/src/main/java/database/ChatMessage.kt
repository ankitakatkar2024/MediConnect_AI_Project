package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages_table")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val message: String,
    val isUserMessage: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)