package com.example.mediconnect_ai

data class SosAlert(
    val patientId: Long,
    val medicalSummary: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val sentTo: String,              // phone number where SMS was sent
    val timestamp: Long = System.currentTimeMillis()
)
