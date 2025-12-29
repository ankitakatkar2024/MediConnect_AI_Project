package com.example.mediconnect_ai

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityReferPatientBinding
import kotlinx.coroutines.launch

class ReferPatientActivity : BaseActivity() {

    private lateinit var binding: ActivityReferPatientBinding
    private var patientId: Long = -1L

    companion object {
        const val EXTRA_PATIENT_ID = "EXTRA_PATIENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)
        if (patientId == -1L) {
            Toast.makeText(this, "Error: Patient ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setSupportActionBar(binding.toolbarReferPatient)
        supportActionBar?.title = "Refer Patient"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmitReferral.setOnClickListener {
            submitReferral()
        }
    }

    private fun submitReferral() {
        val reason = binding.etReferralReason.text.toString().trim()
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason for the referral.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val patientDao = AppDatabase.getInstance(applicationContext).patientDao()
            val patient = patientDao.getPatientById(patientId)
            if (patient != null) {
                // Update the patient's record to flag them as referred
                val updatedPatient = patient.copy(isReferred = true, referralReason = reason)
                patientDao.update(updatedPatient)
                Toast.makeText(this@ReferPatientActivity, "Patient referred successfully.", Toast.LENGTH_LONG).show()
                finish() // Close the referral screen
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
