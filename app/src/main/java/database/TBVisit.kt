package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tb_visit_table",
    foreignKeys = [
        ForeignKey(
            entity = TBProfile::class,
            parentColumns = ["id"],
            childColumns = ["tbProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tbProfileId")]
)
data class TBVisit(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tbProfileId: Long,

    val visitType: String,
    // "Weekly Follow-up", "Bi-weekly Follow-up", "Extra Visit"

    val treatmentPhase: String,
    // "IP" or "CP"

    val dueDate: Long,

    var isCompleted: Boolean = false,

    var completionDate: Long? = null,

    // Adherence & Monitoring
    var medicinesTaken: Boolean? = null,

    var missedDoseCount: Int = 0,

    var symptomsNotes: String? = null,

    var sideEffects: Boolean = false,

    var weightKg: Double? = null,


    val description: String,   // ✅ ADD THIS

    // Escalation
    var needsReferral: Boolean = false
)
