package com.example.mediconnect_ai.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VaccineStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vaccines: List<VaccineStatus>)

    @Update
    suspend fun update(vaccineStatus: VaccineStatus)

    @Query("SELECT * FROM vaccine_status WHERE patientId = :patientId ORDER BY dueDate ASC")
    fun getScheduleForPatient(patientId: Long): LiveData<List<VaccineStatus>>

    @Query("SELECT * FROM vaccine_status WHERE status = 'Scheduled' AND dueDate BETWEEN :startOfWeek AND :endOfWeek")
    suspend fun getUpcomingVaccinations(startOfWeek: Long, endOfWeek: Long): List<VaccineStatus>

    // NEW: A non-LiveData version to get a simple list for background checking.
    // This is required by DailyTasksActivity.
    @Query("SELECT * FROM vaccine_status WHERE patientId = :patientId ORDER BY dueDate ASC")
    suspend fun getScheduleForPatientAsList(patientId: Long): List<VaccineStatus>
}

