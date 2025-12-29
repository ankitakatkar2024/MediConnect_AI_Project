package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : BaseActivity() {

    companion object {
        // ✅ Shared prefs keys (same as MainActivity & WelcomeActivity)
        private const val PREFS_NAME = "MediConnectPrefs"
        private const val KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"
        private const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        private const val KEY_USER_ROLE = "USER_ROLE"
        private const val KEY_USER_MPIN = "USER_MPIN"
        private const val KEY_MPIN_VERIFIED_SESSION = "MPIN_VERIFIED_SESSION"

        private const val EXTRA_NEXT = "NEXT"
        private const val NEXT_LOGIN = "LOGIN"
        private const val NEXT_VERIFY_MPIN = "VERIFY_MPIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // ✅ Step 0: FIRST LAUNCH CHECK → show Welcome only once
        val isFirstLaunch = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        // ✅ STEP 1: For every fresh app launch, force MPIN re-verification
        // (This ensures: Splash → StartScreen → MPIN → Dashboard for logged-in users)
        prefs.edit()
            .putBoolean(KEY_MPIN_VERIFIED_SESSION, false)
            .apply()

        // ✅ STEP 2: Normal splash routing logic
        val auth = FirebaseAuth.getInstance() // (kept if you later want to check auth.currentUser)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val userRole = prefs.getString(KEY_USER_ROLE, null)
        val hasRole = !userRole.isNullOrBlank()
        val hasMpin = !prefs.getString(KEY_USER_MPIN, null).isNullOrBlank()
        val mpinVerifiedThisSession = prefs.getBoolean(KEY_MPIN_VERIFIED_SESSION, false)

        when {
            // 1️⃣ No login + no role → RoleSelection
            !isLoggedIn && !hasRole -> {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
            }

            // 2️⃣ Role chosen but not logged in → StartScreen (LOGIN flow)
            !isLoggedIn && hasRole -> {
                startActivity(Intent(this, StartScreenActivity::class.java).apply {
                    putExtra(EXTRA_NEXT, NEXT_LOGIN)
                })
            }

            // 3️⃣ Logged in → always go to StartScreen to VERIFY MPIN
            //    (mpinVerifiedThisSession was reset to false above)
            isLoggedIn -> {
                startActivity(Intent(this, StartScreenActivity::class.java).apply {
                    putExtra(EXTRA_NEXT, NEXT_VERIFY_MPIN)
                })
            }

            // 4️⃣ Safety fallback → RoleSelection
            else -> {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
            }
        }

        finish()
    }
}
