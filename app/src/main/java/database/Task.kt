package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val patientId: Long,
    val patientName: String,
    val taskDescription: String,
    val dueDate: Long, // The day the task is due
    var isCompleted: Boolean = false
)