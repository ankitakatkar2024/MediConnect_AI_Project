package com.example.mediconnect_ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface  ANCVisitDao {

    // Return generated IDs so we can sync with Firestore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<ANCVisit>): List<Long>

    @Update
    suspend fun update(visit: ANCVisit)

    // Get all visits for a specific pregnancy ordered by date
    @Query("SELECT * FROM anc_visit_table WHERE pregnancyId = :pregnancyId ORDER BY dueDate ASC")
    suspend fun getVisitsForPregnancy(pregnancyId: Long): List<ANCVisit>

    // Get upcoming visits for the daily dashboard
    @Query("SELECT * FROM anc_visit_table WHERE isCompleted = 0 AND dueDate BETWEEN :start AND :end")
    suspend fun getUpcomingANCVisits(start: Long, end: Long): List<ANCVisit>
}
