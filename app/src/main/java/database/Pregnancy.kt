package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pregnancies_table")
data class Pregnancy(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // This links the pregnancy to a patient
    val patientId: Long,

    // We store the patient's name for easy display
    val patientName: String,

    // Last Menstrual Period, stored as a timestamp
    val lmpDate: Long,

    // Expected Delivery Date, stored as a timestamp
    val edd: Long,

    // To track if the pregnancy is completed
    var isActive: Boolean = true,

    // This column links a pregnancy record to a specific ASHA worker.
    var ashaId: String,

    // --- NEW FIELD (Matches Migration 13->14) ---
    // Stores the timestamp when the baby was born.
    // Nullable because a new pregnancy hasn't delivered yet.
    var deliveryDate: Long? = null
)