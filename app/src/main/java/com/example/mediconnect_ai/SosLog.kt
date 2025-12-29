package com.example.mediconnect_ai

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sos_logs")
data class SosLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val contactPhone: String,
    val actionType: String, // "CALL" or "WHATSAPP"
    val profilePhotoResId: Int? = null, // ✅ New field for profile photo drawable resource ID
    val timestamp: Long = System.currentTimeMillis() // ✅ Store as Long for proper sorting
)
