package com.example.mediconnect_ai

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SosLogDao {

    // ✅ Use REPLACE to avoid conflict crashes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SosLog)

    // ✅ Fetch logs with most recent first
    @Query("SELECT * FROM sos_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<SosLog>

    // ✅ Optional: Clear all logs
    @Query("DELETE FROM sos_logs")
    suspend fun clearLogs()
}
