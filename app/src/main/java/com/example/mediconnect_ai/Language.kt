package com.example.mediconnect_ai

// Represents a single language option
data class Language(
    val code: String, // e.g., "en", "hi"
    val nativeName: String, // e.g., "English", "हिन्दी"
    val englishName: String // e.g., "(English)", "(Hindi)"
)
