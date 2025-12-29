package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Managed by ASHA for follow-up; official reporting remains in Nikshay

@Entity(
    tableName = "tb_profile_table",
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
data class TBProfile(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val patientId: Long,

    val nikshayId: String?, // Filled after PHC registration

    val diagnosisDate: Long,

    val tbType: String,
    // Pulmonary / Extra-Pulmonary

    val drugResistanceType: String = "DS",
    // DS / MDR / XDR

    val treatmentStartDate: Long,

    val continuationPhaseStartDate: Long?,
    // Auto-calculated (usually start + 60 days)

    val treatmentDurationMonths: Int = 6,

    val treatmentPhase: String,
    // "IP" or "CP"

    val lastFollowUpDate: Long?,

    val adherenceRisk: Boolean = false,
    // Flagged if missed doses / side effects

    var isActive: Boolean = true,

    var outcome: String? = null,
    // Cured / Completed / Defaulted

    val dbtStatus: String? = null
    // Received / Pending / Not Eligible
)
