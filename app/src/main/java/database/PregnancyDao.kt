package com.example.mediconnect_ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PregnancyDao {

    // 1. Insert new pregnancy
    @Insert
    suspend fun insert(pregnancy: Pregnancy): Long

    // 2. Update existing pregnancy (needed for Register Delivery)
    @Update
    suspend fun update(pregnancy: Pregnancy)

    // 3. Get the currently active pregnancy for a patient
    @Query("SELECT * FROM pregnancies_table WHERE patientId = :patientId AND isActive = 1 LIMIT 1")
    suspend fun getActivePregnancyForPatient(patientId: Long): Pregnancy?

    // 4. Get all active pregnancies
    @Query("SELECT * FROM pregnancies_table WHERE isActive = 1")
    suspend fun getActivePregnancies(): List<Pregnancy>

    // 5. Get active pregnancies assigned to specific ASHA worker
    @Query("SELECT * FROM pregnancies_table WHERE isActive = 1 AND ashaId = :userId")
    suspend fun getActivePregnanciesForUser(userId: String): List<Pregnancy>

    // 6. Get pregnancy by ID (useful for some operations)
    @Query("SELECT * FROM pregnancies_table WHERE id = :id LIMIT 1")
    suspend fun getPregnancyById(id: Long): Pregnancy?


        // ADD THIS MISSING QUERY:
        @Query("SELECT * FROM pregnancies_table WHERE patientId = :patientId")
        suspend fun getAllPregnanciesForPatient(patientId: Long): List<Pregnancy>
    }



