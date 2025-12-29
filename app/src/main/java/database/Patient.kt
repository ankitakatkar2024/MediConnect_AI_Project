package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients_table")
data class Patient(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Basic Info
    val fullName: String,
    val dob: Long,                 // DOB in millis
    val gender: String,
    val contactNumber: String,
    val address: String,
    val preExistingConditions: String,
    val aadhaarNumber: String,

    // Registration timestamp
    val registrationTimestamp: Long = System.currentTimeMillis(),

    // Referral System
    var isReferred: Boolean = false,
    var referralReason: String? = null,
    var isResolved: Boolean = false,

    // ASHA Worker (⚠️ default added to avoid insert crashes)
    var assignedAshaId: String = "",

    // --- NEW FIELDS FOR HOUSEHOLD / FAMILY ---
    val familyId: String? = null,       // same for all members of one home
    val householdSize: Int? = null,     // total people in house
    val isHeadOfFamily: Boolean = false,

    // Smart Maps (GPS)
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
