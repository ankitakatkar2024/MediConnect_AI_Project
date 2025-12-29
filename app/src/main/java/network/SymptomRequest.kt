package com.example.mediconnect_ai.network

// This class defines the JSON object we send TO the server.
// e.g., {"symptom": "fever and headache"}
data class SymptomRequest(
    val symptom: String,
    val language: String // NEW: Add this field
)