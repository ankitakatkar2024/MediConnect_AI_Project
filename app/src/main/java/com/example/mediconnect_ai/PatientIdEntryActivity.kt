package com.example.mediconnect_ai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.mediconnect_ai.databinding.ActivityPatientIdEntryBinding

class PatientIdEntryActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientIdEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientIdEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Verify Patient ID"

        binding.btnConfirmId.setOnClickListener {
            val patientIdString = binding.etPatientId.text.toString().trim()
            if (patientIdString.isNotEmpty()) {
                try {
                    val patientId = patientIdString.toLong()
                    // If the ID is valid, launch the dashboard and pass the ID to it
                    val intent = Intent(this, PatientDashboardActivity::class.java)
                    intent.putExtra(PatientDashboardActivity.EXTRA_PATIENT_ID, patientId)
                    startActivity(intent)
                    finish() // Close this screen so the user can't come back
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid numeric ID.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter your Patient ID.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
