package com.example.mediconnect_ai

data class EmergencyContact(
    val name: String,
    val role: String,
    val phone: String,
    val profilePhotoResId: Int? = null // ✅ Add this new field
)
