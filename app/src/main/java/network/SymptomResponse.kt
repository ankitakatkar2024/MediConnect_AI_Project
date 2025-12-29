package com.example.mediconnect_ai.network

// This class defines the JSON object we expect back FROM the server.
// e.g., {"suggestion": "You may have a viral infection..."}
data class SymptomResponse(
    val suggestion: String
)