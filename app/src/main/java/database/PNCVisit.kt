package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pnc_visit_table",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class PNCVisit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val visitName: String, // e.g., "Day 3 Visit"
    val dayNumber: Int,    // 1, 3, 7, 14, etc.
    val dueDate: Long,     // Calculated based on Delivery Date
    var isCompleted: Boolean = false,
    var completionDate: Long? = null,

    // Health Checks
    var motherBP: String? = null,
    var motherBleeding: Boolean = false,
    var babyWeight: Double? = 0.0,
    var babyFeeding: String? = null // "Exclusive Breastfeeding", "Formula", etc.
)