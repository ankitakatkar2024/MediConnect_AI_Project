package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.mediconnect_ai.databinding.ActivityMpinBinding

class MPinActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMpinBinding
    private lateinit var pinDots: List<ImageView>
    private lateinit var keypadButtons: List<View>

    private val pin = StringBuilder()
    private val pinLength = 4
    private var mode: String? = null
    private var savedRole: String? = null
    private var storedPin: String? = null

    companion object {
        private const val PREFS_NAME = "MediConnectPrefs"
        private const val KEY_USER_MPIN = "USER_MPIN"
        private const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        private const val KEY_USER_ROLE = "USER_ROLE"
        private const val KEY_MPIN_VERIFIED_SESSION = "MPIN_VERIFIED_SESSION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMpinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MODE and ROLE from previous screen (if provided)
        mode = intent.getStringExtra("MODE")
        savedRole = intent.getStringExtra("USER_ROLE")

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        storedPin = prefs.getString(KEY_USER_MPIN, null)

        // If MODE not provided, decide automatically
        if (mode.isNullOrBlank()) {
            mode = if (storedPin.isNullOrEmpty()) "SET" else "VERIFY"
        }

        // If we are in VERIFY mode but no PIN exists yet, force SET mode
        if (mode == "VERIFY" && storedPin.isNullOrEmpty()) {
            mode = "SET"
        }

        initializeViews()
        setupClickListeners()

        // Initial message
        binding.messageTextView.text = if (mode == "SET") {
            getString(R.string.mpin_message_set_new)
        } else {
            getString(R.string.mpin_title_enter_pin)
        }
    }

    private fun initializeViews() {
        pinDots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        keypadButtons = listOf(
            binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6,
            binding.btn7, binding.btn8, binding.btn9,
            binding.btn0, binding.btnBackspace
        )
    }

    private fun setupClickListeners() {
        keypadButtons.forEach { it.setOnClickListener(this) }
        binding.submitButton.setOnClickListener { handleSubmit() }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBackspace -> handleBackspace()
            else -> {
                if (v is Button) {
                    handleKeyPress(v.text.toString())
                }
            }
        }
    }

    private fun handleKeyPress(number: String) {
        if (pin.length < pinLength) {
            pin.append(number)
            updatePinDisplay()
        }
    }

    private fun handleBackspace() {
        if (pin.isNotEmpty()) {
            pin.deleteCharAt(pin.length - 1)
            updatePinDisplay()
        }
    }

    private fun updatePinDisplay() {
        for (i in pinDots.indices) {
            pinDots[i].isActivated = i < pin.length
        }
        binding.submitButton.isEnabled = pin.length == pinLength
    }

    private fun handleSubmit() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enteredPin = pin.toString()

        setKeypadEnabled(false)

        if (mode == "SET") {
            // Save new PIN and mark as logged in + MPIN verified for this session
            prefs.edit()
                .putString(KEY_USER_MPIN, enteredPin)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putBoolean(KEY_MPIN_VERIFIED_SESSION, true)
                .apply()

            showMessage(getString(R.string.mpin_set_successfully), isError = false)
            navigateToDashboardWithDelay(isSuccess = true)

        } else if (mode == "VERIFY") {
            if (enteredPin == storedPin) {
                // Correct PIN: mark as logged in + MPIN verified
                prefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .putBoolean(KEY_MPIN_VERIFIED_SESSION, true)
                    .apply()

                showMessage(getString(R.string.mpin_correct_welcome), isError = false)
                navigateToDashboardWithDelay(isSuccess = true)
            } else {
                showMessage(getString(R.string.mpin_incorrect_try_again), isError = true)
                Handler(Looper.getMainLooper()).postDelayed({
                    resetPin()
                }, 1500)
            }
        }
    }

    private fun navigateToDashboardWithDelay(isSuccess: Boolean) {
        if (isSuccess) {
            pinDots.forEach { it.setImageResource(R.drawable.pin_dot_success) }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Prefer role from Intent, else fallback to saved role in prefs
            val intentRole = savedRole
            val role = if (!intentRole.isNullOrBlank()) {
                intentRole
            } else {
                prefs.getString(KEY_USER_ROLE, null)
            }

            // If we received a role, store it for consistency
            if (!role.isNullOrBlank()) {
                prefs.edit().putString(KEY_USER_ROLE, role).apply()
            }

            val destination = when (role) {
                "asha_anm_nurse" -> MainActivity::class.java
                "doctor" -> DoctorDashboardActivity::class.java
                "patient_family" -> PatientIdEntryActivity::class.java
                else -> RoleSelectionActivity::class.java
            }

            // Clear back stack so we don’t go back to Splash or StartScreen
            val nextIntent = Intent(this, destination).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(nextIntent)
            finish()
        }, 1000)
    }

    private fun showMessage(message: String, isError: Boolean) {
        binding.messageTextView.text = message
        binding.messageTextView.setTextColor(
            ContextCompat.getColor(
                this,
                if (isError) R.color.error_red else R.color.success_green
            )
        )
    }

    private fun resetPin() {
        pin.clear()
        updatePinDisplay()
        binding.messageTextView.text =
            if (mode == "SET") getString(R.string.mpin_message_set_new)
            else getString(R.string.mpin_title_enter_pin)

        binding.messageTextView.setTextColor(
            ContextCompat.getColor(this, R.color.success_green)
        )
        setKeypadEnabled(true)
    }

    private fun setKeypadEnabled(isEnabled: Boolean) {
        keypadButtons.forEach { it.isEnabled = isEnabled }
        binding.submitButton.isEnabled = if (isEnabled) pin.length == pinLength else false
    }
}
