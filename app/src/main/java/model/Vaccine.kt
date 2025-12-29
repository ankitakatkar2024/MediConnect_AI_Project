package com.example.mediconnect_ai.model

import java.util.Date

/**
 * Represents a single vaccine in the immunization schedule.
 * This data class holds all the necessary information about a vaccine.
 */
data class Vaccine(
    // The official name of the vaccine, e.g., "BCG".
    val name: String,

    // A brief explanation of why the vaccine is important, for the ASHA worker to explain.
    val importance: String,

    // The calculated date on which this vaccine is due.
    val dueDate: Date,

    // The current status of the vaccine. 'var' is used because this can change.
    var status: VaccineStatus = VaccineStatus.SCHEDULED
)

/**
 * A type-safe way to represent the status of a vaccine. Using an enum prevents
 * errors from typos that could happen if we used simple Strings like "overdue".
 */
enum class VaccineStatus {
    SCHEDULED, // The vaccine is due in the future.
    UPCOMING,  // The vaccine is due within the next 7 days.
    OVERDUE    // The due date for the vaccine has passed.
}

