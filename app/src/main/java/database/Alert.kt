package com.example.mediconnect_ai

data class Alert(
    val title: String = "",
    val message: String = "",
    val targetAudience: String = "all",   // "all", "pregnant", "children_under_5"
    val senderId: String = "admin_test",  // later you can use FirebaseAuth UID
    val timestamp: Long = System.currentTimeMillis()
)
