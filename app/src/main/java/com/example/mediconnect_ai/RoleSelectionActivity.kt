package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class RoleSelectionActivity :  BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val prefs = getSharedPreferences("MediConnectPrefs", Context.MODE_PRIVATE)

        // RoleSelectionActivity should only be shown:
        // 1. On first install (no role saved)
        // 2. After logout (role removed)
        val savedRole = prefs.getString("USER_ROLE", null)
        if (!savedRole.isNullOrEmpty()) {
            // If somehow reached here with a role, skip to StartScreenActivity
            startActivity(Intent(this, StartScreenActivity::class.java))
            finish()
            return
        }

        // Role selection actions
        findViewById<ConstraintLayout>(R.id.btnRoleDoctor).setOnClickListener {
            saveRoleAndGoToStartScreen("doctor")
        }
        findViewById<ConstraintLayout>(R.id.btnRoleAsha).setOnClickListener {
            saveRoleAndGoToStartScreen("asha_anm_nurse")
        }
        findViewById<ConstraintLayout>(R.id.btnRolePatient).setOnClickListener {
            saveRoleAndGoToStartScreen("patient_family")
        }
    }

    private fun saveRoleAndGoToStartScreen(role: String) {
        val prefs = getSharedPreferences("MediConnectPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("USER_ROLE", role).apply()

        val intent = Intent(this, StartScreenActivity::class.java)
        // After role selection → first time flag
        intent.putExtra("IS_FIRST_TIME", true)
        startActivity(intent)
        finish()
    }

    companion object {
        fun performLogout(context: Context) {
            val prefs = context.getSharedPreferences("MediConnectPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .remove("IS_LOGGED_IN")
                .remove("USER_ROLE")
                .apply()

            // After logout, always go back to RoleSelectionActivity
            val intent = Intent(context, RoleSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }
}
