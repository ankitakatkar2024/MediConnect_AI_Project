package com.example.mediconnect_ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.firestore.FirebaseAlertHelper
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Calendar

class SendAlertActivity : BaseActivity() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etBody: TextInputEditText
    private lateinit var rgTarget: RadioGroup
    private lateinit var btnSend: Button
    private lateinit var tvStatus: TextView

    // Permission Launcher for SMS
    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                performBroadcast()
            } else {
                Toast.makeText(this, "SMS Permission Required to Broadcast", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_alert)

        // Setup Toolbar Back Button
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        // Bind Views
        etTitle = findViewById(R.id.etTitle)
        etBody = findViewById(R.id.etBody)
        rgTarget = findViewById(R.id.rgTarget)
        btnSend = findViewById(R.id.btnSendAlert)
        tvStatus = findViewById(R.id.tvRecipientCount)

        btnSend.setOnClickListener {
            // Check Permission before sending
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                performBroadcast()
            } else {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    private fun performBroadcast() {
        val title = etTitle.text.toString().trim()
        val message = etBody.text.toString().trim()

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please enter a title and message", Toast.LENGTH_SHORT).show()
            return
        }

        // Format the SMS content
        val fullMessage = "📢 ALERT: $title\n$message\n- Sent via MediConnect AI"

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)

            // Use a Set to store unique numbers
            val phoneNumbers = mutableSetOf<String>()

            // 1. Determine Target Audience based on Radio Button Selection
            val selectedId = rgTarget.checkedRadioButtonId

            // We’ll also store a code string for Firestore
            val targetAudienceCode = when (selectedId) {
                R.id.rbAll -> {
                    val allPatients = db.patientDao().getAllPatients()
                    allPatients.forEach {
                        if (it.contactNumber.length >= 10) phoneNumbers.add(it.contactNumber)
                    }
                    "all"
                }
                R.id.rbPregnant -> {
                    val activePregnancies = db.pregnancyDao().getActivePregnancies()
                    activePregnancies.forEach { pregnancy ->
                        val patient = db.patientDao().getPatientById(pregnancy.patientId)
                        if (patient != null && patient.contactNumber.length >= 10) {
                            phoneNumbers.add(patient.contactNumber)
                        }
                    }
                    "pregnant"
                }
                R.id.rbChildren -> {
                    val fiveYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -5) }.timeInMillis
                    val children = db.patientDao().getPatientsBornAfter(fiveYearsAgo)
                    children.forEach {
                        if (it.contactNumber.length >= 10) phoneNumbers.add(it.contactNumber)
                    }
                    "children_under_5"
                }
                else -> "all"
            }

            // 2. Send SMS Loop
            if (phoneNumbers.isNotEmpty()) {
                tvStatus.text = "Sending to ${phoneNumbers.size} recipients..."
                var sentCount = 0

                for (number in phoneNumbers) {
                    sendSms(number, fullMessage)
                    sentCount++
                }

                // 3. Log this broadcast to Firestore (one document, not per number)
                val alert = Alert(
                    title = title,
                    message = message,
                    targetAudience = targetAudienceCode,
                    senderId = "admin_test", // or ASHA user id when you have auth
                    timestamp = System.currentTimeMillis()
                )

                FirebaseAlertHelper.saveAlert(alert) { success, error ->
                    // Optional: you can show a toast on failure
                    if (!success) {
                        // Toast.makeText(this, "Cloud alert log failed: $error", Toast.LENGTH_SHORT).show()
                    }
                }

                Toast.makeText(
                    this@SendAlertActivity,
                    "✅ Broadcast Sent to $sentCount families!",
                    Toast.LENGTH_LONG
                ).show()

                // Clear inputs after successful send
                etTitle.text?.clear()
                etBody.text?.clear()
                tvStatus.text = "Broadcast complete. Sent to $sentCount numbers."
            } else {
                Toast.makeText(
                    this@SendAlertActivity,
                    "No valid phone numbers found for this category.",
                    Toast.LENGTH_SHORT
                ).show()
                tvStatus.text = "No recipients found."
            }
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            // Android 12+ (API 31) support
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
            } else {
                SmsManager.getDefault()
            }

            // Split long messages automatically
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
