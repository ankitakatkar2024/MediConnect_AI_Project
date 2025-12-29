package com.example.mediconnect_ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TBDao {

    // ================= TB PROFILE OPERATIONS =================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: TBProfile): Long

    @Update
    suspend fun updateProfile(profile: TBProfile)

    @Query("""
        SELECT * FROM tb_profile_table 
        WHERE patientId = :patientId AND isActive = 1
        LIMIT 1
    """)
    suspend fun getActiveTBProfile(patientId: Long): TBProfile?

    @Query("""
        SELECT * FROM tb_profile_table
        WHERE isActive = 1 AND adherenceRisk = 1
    """)
    suspend fun getHighRiskTBProfiles(): List<TBProfile>

    // ✅ REQUIRED BY DailyTasksActivity
    @Query("""
        SELECT * FROM tb_profile_table
        WHERE id = :profileId
        LIMIT 1
    """)
    suspend fun getTBProfileById(profileId: Long): TBProfile?

    // ================= TB VISIT OPERATIONS =================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisits(visits: List<TBVisit>)

    @Update
    suspend fun updateVisit(visit: TBVisit)

    // ✅ REQUIRED BY DailyTasksActivity
    @Update
    suspend fun updateTBVisit(visit: TBVisit)

    @Query("""
        UPDATE tb_visit_table
        SET isCompleted = 1,
            completionDate = :completionDate
        WHERE id = :visitId
    """)
    suspend fun markVisitCompleted(
        visitId: Long,
        completionDate: Long
    )

    @Query("""
        SELECT * FROM tb_visit_table
        WHERE tbProfileId = :profileId
        ORDER BY dueDate ASC
    """)
    suspend fun getVisitsForProfile(profileId: Long): List<TBVisit>

    @Query("""
        SELECT * FROM tb_visit_table
        WHERE tbProfileId = :profileId
        AND treatmentPhase = :phase
        ORDER BY dueDate ASC
    """)
    suspend fun getVisitsByPhase(
        profileId: Long,
        phase: String
    ): List<TBVisit>

    // ================= DAILY TASK SUPPORT =================

    // ✅ REQUIRED BY DailyTasksActivity
    @Query("""
        SELECT * FROM tb_visit_table
        WHERE isCompleted = 0
        AND dueDate >= :startOfDay
        AND dueDate < :endOfDay
    """)
    suspend fun getTBVisitsDueToday(
        startOfDay: Long,
        endOfDay: Long
    ): List<TBVisit>

    // Existing (kept)
    @Query("""
        SELECT v.* FROM tb_visit_table v
        INNER JOIN tb_profile_table p
        ON v.tbProfileId = p.id
        WHERE v.isCompleted = 0
        AND p.isActive = 1
        AND v.dueDate BETWEEN :start AND :end
        ORDER BY v.dueDate ASC
    """)
    suspend fun getTodayTBVisits(
        start: Long,
        end: Long
    ): List<TBVisit>

    @Query("""
        SELECT v.* FROM tb_visit_table v
        INNER JOIN tb_profile_table p
        ON v.tbProfileId = p.id
        WHERE v.isCompleted = 0
        AND p.isActive = 1
        AND v.dueDate < :today
        ORDER BY v.dueDate ASC
    """)
    suspend fun getOverdueTBVisits(today: Long): List<TBVisit>

    @Query("""
        SELECT * FROM tb_visit_table
        WHERE needsReferral = 1
        AND isCompleted = 0
    """)
    suspend fun getVisitsNeedingReferral(): List<TBVisit>
}
