package com.example.mediconnect_ai.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "anc_visit_table",
    foreignKeys = [
        ForeignKey(
            entity = Pregnancy::class,
            parentColumns = ["id"],
            childColumns = ["pregnancyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pregnancyId")]
)
data class ANCVisit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pregnancyId: Long, // Links to the specific pregnancy record
    val visitName: String, // e.g., "1st Trimester Visit"
    val dueWeekStart: Int, // e.g., 14
    val dueWeekEnd: Int,   // e.g., 26
    val dueDate: Long,     // The calculated ideal date based on LMP
    var isCompleted: Boolean = false,
    var completionDate: Long? = null,
    var notes: String? = null
)