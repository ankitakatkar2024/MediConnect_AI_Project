package com.example.mediconnect_ai.database

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow



@Dao
interface PatientDao {
    @Insert
    suspend fun insert(patient: Patient): Long

    @Update
    suspend fun update(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)

    @Query("SELECT * FROM patients_table ORDER BY fullName ASC")
    suspend fun getAllPatients(): List<Patient>

    @Query("SELECT * FROM patients_table WHERE id = :patientId")
    suspend fun getPatientById(patientId: Long): Patient?

    @Query("SELECT COUNT(id) FROM patients_table")
    suspend fun getTotalPatientCount(): Int

    @Query("SELECT COUNT(id) FROM patients_table WHERE registrationTimestamp >= :startOfDay AND registrationTimestamp < :endOfDay")
    suspend fun getPatientsRegisteredTodayCount(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT * FROM patients_table WHERE dob >= :minDob")
    suspend fun getPatientsBornAfter(minDob: Long): List<Patient>

    // Return referred AND not-yet-resolved patients (LiveData so UI updates automatically)
    @Query("SELECT * FROM patients_table WHERE isReferred = 1 AND (isResolved = 0 OR isResolved IS NULL) ORDER BY fullName ASC")
    fun getReferredPatients(): LiveData<List<Patient>>

    // Mark a patient resolved by id
    @Query("UPDATE patients_table SET isResolved = 1 WHERE id = :id")
    suspend fun markResolvedById(id: Long)

    // --- ADDED: Functions to filter patient data by the logged-in user's ID ---
    @Query("SELECT COUNT(*) FROM patients_table WHERE assignedAshaId = :userId")
    suspend fun getTotalPatientCountForUser(userId: String): Int

    @Query("SELECT * FROM patients_table WHERE dob > :timestamp AND assignedAshaId = :userId")
    suspend fun getInfantsTrackedForUser(userId: String, timestamp: Long): List<Patient>

    // ✅ NEW: Fetch patients grouped by Family ID
    // Ordered so Head of Family appears first inside each family
    @Query("""
        SELECT * FROM patients_table
        ORDER BY familyId ASC, isHeadOfFamily DESC, fullName ASC
    """)
    fun getAllPatientsGroupedByFamily(): Flow<List<Patient>>
}
