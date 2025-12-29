package com.example.mediconnect_ai.models

/**
 * A unified model to represent any event in the patient's life.
 */
data class HistoryEvent(
    val dateTimestamp: Long,
    val eventType: EventType, // Enum: REGISTRATION, PREGNANCY, VACCINE, VISIT, ALERT
    val title: String,        // e.g., "OPV-1 Vaccine"
    val description: String,  // e.g., "Administered by ASHA"
    val status: String        // "Completed", "Pending", "Overdue"
)

enum class EventType {
    REGISTRATION,
    PREGNANCY_START,
    DELIVERY,
    ANC_VISIT,
    PNC_VISIT,
    VACCINE,
    SYMPTOM_LOG,
    ALERT  // <--- ADD THIS LINE
}