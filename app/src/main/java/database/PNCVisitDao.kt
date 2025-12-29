package com.example.mediconnect_ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PNCVisitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<PNCVisit>)

    @Update
    suspend fun update(visit: PNCVisit)

    @Query("SELECT * FROM pnc_visit_table WHERE patientId = :patientId ORDER BY dueDate ASC")
    suspend fun getPNCVisitsForPatient(patientId: Long): List<PNCVisit>

    // For Daily Tasks Dashboard
    // Use half-open interval: [start, end)
    @Query("""
        SELECT * FROM pnc_visit_table 
        WHERE isCompleted = 0 
          AND dueDate >= :start 
          AND dueDate < :end
        ORDER BY dueDate ASC
    """)
    suspend fun getUpcomingPNCVisits(start: Long, end: Long): List<PNCVisit>
}
