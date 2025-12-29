package com.example.mediconnect_ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long   // ⬅ return Long instead of Unit

    @Update
    suspend fun update(task: Task)

    @Query("SELECT * FROM tasks_table WHERE dueDate >= :startOfDay AND dueDate < :endOfDay ORDER BY isCompleted ASC, dueDate ASC")
    suspend fun getTasksForToday(startOfDay: Long, endOfDay: Long): List<Task>

    @Query("SELECT * FROM tasks_table WHERE patientId = :patientId ORDER BY dueDate DESC")
    suspend fun getTasksForPatient(patientId: Long): List<Task>

    @Query("SELECT COUNT(id) FROM tasks_table WHERE dueDate >= :startOfDay AND dueDate < :endOfDay AND isCompleted = 0")
    suspend fun getIncompleteTasksForTodayCount(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM tasks_table WHERE patientId = :patientId AND taskDescription = :description AND dueDate = :dueDate LIMIT 1)")
    suspend fun taskExists(patientId: Long, description: String, dueDate: Long): Boolean
}
