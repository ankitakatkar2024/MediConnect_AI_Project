package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class StartScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        val prefs = getSharedPreferences("MediConnectPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)
        val savedRole = prefs.getString("USER_ROLE", null)
        val storedPin = prefs.getString("USER_MPIN", null)

        // ✅ Find the button first
        val startButton = findViewById<MaterialButton>(R.id.btnStartUsingApp)

        // ✅ FIXED: Add the emoji programmatically to prevent build errors
        // This line takes the clean text from strings.xml and adds the emoji before displaying it.
        startButton.text = "🌿 ${getString(R.string.start_screen_button)}"

        // ✅ Set the click listener on the button variable
        startButton.setOnClickListener {
            if (isLoggedIn) {
                // If the user is already logged in, check if they need to set or verify an MPIN
                val intent = if (storedPin.isNullOrEmpty()) {
                    // Go to MPIN setup
                    Intent(this, MPinActivity::class.java).apply {
                        putExtra("MODE", "SET")
                        putExtra("USER_ROLE", savedRole)
                    }
                } else {
                    // Go to MPIN verification
                    Intent(this, MPinActivity::class.java).apply {
                        putExtra("MODE", "VERIFY")
                        putExtra("USER_ROLE", savedRole)
                    }
                }
                startActivity(intent)
            } else {
                // If the user is not logged in, determine if they need to select a role or go to the login page
                val intent = if (savedRole.isNullOrEmpty()) {
                    Intent(this, RoleSelectionActivity::class.java)
                } else {
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(intent)
            }
            finish()
        }
    }
}

