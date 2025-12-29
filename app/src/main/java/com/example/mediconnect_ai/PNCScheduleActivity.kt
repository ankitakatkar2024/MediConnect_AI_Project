package com.example.mediconnect_ai

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.PNCVisitAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.PNCVisit
import com.example.mediconnect_ai.databinding.ActivityPncScheduleBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PNCScheduleActivity : BaseActivity() {

    private lateinit var binding: ActivityPncScheduleBinding
    private lateinit var adapter: PNCVisitAdapter
    private var patientId: Long = -1L

    companion object {
        const val EXTRA_PATIENT_ID = "PATIENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPncScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Get Patient ID passed from PatientDetailActivity
        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)
        if (patientId == -1L) {
            Toast.makeText(this, "Error: Patient ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadPNCData()
    }

    private fun setupRecyclerView() {
        // Initialize adapter with the click listener for "Mark Completed"
        adapter = PNCVisitAdapter { visit ->
            markVisitAsCompleted(visit)
        }
        binding.recyclerViewPNC.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPNC.adapter = adapter
    }

    private fun loadPNCData() {
        val db = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            // 1. Fetch Patient Details for the header
            val patient = db.patientDao().getPatientById(patientId)

            // 2. Fetch all PNC Visits associated with this patient
            val visits = db.pncVisitDao().getPNCVisitsForPatient(patientId)

            if (patient != null && visits.isNotEmpty()) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

                // Update Header UI
                binding.tvPatientName.text = patient.fullName

                // We use the due date of the first visit (Day 1) as the reference for birth date
                val birthDate = visits[0].dueDate
                // PNC ends 42 days after birth
                val pncEnd = birthDate + (42L * 24 * 60 * 60 * 1000)

                binding.tvDeliveryDate.text = "Born: ${sdf.format(Date(birthDate))}"
                binding.tvPncEndDate.text = "PNC Ends: ${sdf.format(Date(pncEnd))}"

                // Update the List
                adapter.submitList(visits)
            } else {
                Toast.makeText(this@PNCScheduleActivity, "No PNC schedule found.", Toast.LENGTH_SHORT).show()
                // Optional: You could redirect to register delivery if list is empty
            }
        }
    }

    private fun markVisitAsCompleted(visit: PNCVisit) {
        val db = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            // Create a copy of the visit with updated status
            val updatedVisit = visit.copy(
                isCompleted = true,
                completionDate = System.currentTimeMillis()
            )

            // Update in Database
            db.pncVisitDao().update(updatedVisit)

            Toast.makeText(this@PNCScheduleActivity, "Visit Completed! ✅", Toast.LENGTH_SHORT).show()

            // Refresh the list to update colors (Yellow -> Green)
            loadPNCData()
        }
    }
}