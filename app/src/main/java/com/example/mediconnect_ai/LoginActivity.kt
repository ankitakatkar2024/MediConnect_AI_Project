package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoginActivity : BaseActivity() {

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cbRememberMe: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnPlayIntro: FloatingActionButton
    private lateinit var btnEnglish: MaterialButton
    private lateinit var btnHindi: MaterialButton
    private lateinit var btnMarathi: MaterialButton

    private lateinit var tts: TextToSpeech
    private var isPlaying = false
    private lateinit var currentLang: String

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        currentLang = LocaleManager.getPersistedLanguage(this)
        findViewById<View>(R.id.loginCard)
            ?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        // --- Firebase init ---
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // --- Bind UI ---
        btnEnglish = findViewById(R.id.btnEnglish)
        btnHindi = findViewById(R.id.btnHindi)
        btnMarathi = findViewById(R.id.btnMarathi)
        btnPlayIntro = findViewById(R.id.btnPlayIntro)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.pinInputLayout)
        etEmail = findViewById(R.id.inputEmail)
        etPassword = findViewById(R.id.etPin)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.linkForgotPassword)

        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        tvAppName.text = "🌿 ${getString(R.string.app_name)}"
        emailInputLayout.hint = "📧 ${getString(R.string.email_mobile)}"
        passwordInputLayout.hint = "🔒 ${getString(R.string.pin_label)}"

        // --- Registration redirect ---
        findViewById<TextView>(R.id.tvRegister).setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        // --- Language setup ---
        updateLanguageSelection(getButtonByLang(currentLang))
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) setTTSLanguage(currentLang)
        }
        btnEnglish.setOnClickListener { onLanguageChosen("en") }
        btnHindi.setOnClickListener { onLanguageChosen("hi") }
        btnMarathi.setOnClickListener { onLanguageChosen("mr") }
        btnPlayIntro.setOnClickListener {
            if (!isPlaying) playIntroVoice() else stopIntroVoice()
        }

        // --- Email validation ---
        etEmail.addTextChangedListener { text ->
            val value = text?.toString()?.trim().orEmpty()
            emailInputLayout.error = when {
                value.isEmpty() -> null
                android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> null
                else -> getString(R.string.error_email_mobile)
            }
        }

        etPassword.transformationMethod = PasswordTransformationMethod.getInstance()

        // --- Login button ---
        btnLogin.setOnClickListener {
            handleLogin()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, getString(R.string.coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    // 🔥 Real Firebase-Connected Login Logic
    private fun handleLogin() {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString()?.trim().orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        btnLogin.isEnabled = false
        Toast.makeText(this, "Authenticating...", Toast.LENGTH_SHORT).show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val userId = firebaseUser.uid

                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val fetchedUserName = document.getString("fullName") ?: "Guest User"
                                val fetchedUserRole = document.getString("role") ?: "asha_anm_nurse"
                                val fetchedUserArea = document.getString("district") ?: "Unassigned"
                                val fetchedCustomId = document.getString("customId") ?: firebaseUser.uid // 🆕

                                val prefs = getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
                                prefs.edit().apply {
                                    putBoolean("IS_LOGGED_IN", true)
                                    putString(ProfileActivity.KEY_USER_ID, fetchedCustomId) // 🆕 Use short ID
                                    putString(ProfileActivity.KEY_USER_NAME, fetchedUserName)
                                    putString(ProfileActivity.KEY_USER_ROLE, fetchedUserRole)
                                    putString(ProfileActivity.KEY_ASSIGNED_AREA, fetchedUserArea)
                                    apply()
                                }


                                // ✅ Check MPIN or go to dashboard
                                val storedPin = prefs.getString("USER_MPIN", null)
                                if (storedPin.isNullOrEmpty()) {
                                    val intent = Intent(this, MPinActivity::class.java)
                                    intent.putExtra("MODE", "SET")
                                    intent.putExtra("USER_ROLE", fetchedUserRole)
                                    startActivity(intent)
                                } else {
                                    goToDashboard(fetchedUserRole)
                                }
                                finish()
                            } else {
                                Toast.makeText(this, "User record not found in database.", Toast.LENGTH_SHORT).show()
                                btnLogin.isEnabled = true
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to fetch user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            btnLogin.isEnabled = true
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                btnLogin.isEnabled = true
            }
    }

    private fun updateLanguageSelection(selectedButton: MaterialButton) {
        val allButtons = listOf(btnEnglish, btnHindi, btnMarathi)
        for (button in allButtons) {
            if (button == selectedButton) {
                button.isSelected = true
                button.setTextColor(ContextCompat.getColor(this, R.color.text_selected))
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_selected))
            } else {
                button.isSelected = false
                button.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_unselected))
            }
        }
    }

    private fun onLanguageChosen(lang: String) {
        LocaleManager.setNewLocale(this, lang)
    }

    private fun getButtonByLang(lang: String): MaterialButton =
        when (lang) {
            "hi" -> btnHindi
            "mr" -> btnMarathi
            else -> btnEnglish
        }

    private fun setTTSLanguage(lang: String) {
        val locale = Locale(lang)
        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, getString(R.string.tts_lang_not_supported), Toast.LENGTH_SHORT).show()
        }
        tts.setPitch(1.1f)
        tts.setSpeechRate(0.95f)
    }

    private fun playIntroVoice() {
        setTTSLanguage(currentLang)
        val text = getString(R.string.app_intro_text)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        btnPlayIntro.setImageResource(R.drawable.ic_volume_off)
        isPlaying = true
    }

    private fun stopIntroVoice() {
        tts.stop()
        btnPlayIntro.setImageResource(R.drawable.ic_volume_up)
        isPlaying = false
    }

    private fun goToDashboard(role: String) {
        when (role) {
            "asha_anm_nurse" -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            "doctor" -> {
                startActivity(Intent(this, DoctorDashboardActivity::class.java))
            }
            "patient_family" -> {
                // ✅ Ask for Patient ID first
                startActivity(Intent(this, PatientIdEntryActivity::class.java))
            }
            else -> {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
    }
}
