package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "vaccine_status",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VaccineStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val patientId: Long,
    val vaccineName: String,
    val dueDate: Long,

    // Can be: "Scheduled", "Completed", "Overdue"
    var status: String = "Scheduled"
)
