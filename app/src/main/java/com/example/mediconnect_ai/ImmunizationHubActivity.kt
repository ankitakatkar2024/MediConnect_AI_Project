package com.example.mediconnect_ai

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapter.InfantAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityImmunizationHubBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class ImmunizationHubActivity : BaseActivity() {

    private lateinit var binding: ActivityImmunizationHubBinding
    private lateinit var infantAdapter: InfantAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImmunizationHubBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Immunization Schedules"

        setupRecyclerView()
        loadInfants()
    }

    private fun setupRecyclerView() {
        infantAdapter = InfantAdapter { patient ->
            // When an infant is clicked, open their specific schedule
            val intent = Intent(this, ImmunizationScheduleActivity::class.java)
            intent.putExtra(ImmunizationScheduleActivity.EXTRA_PATIENT_ID, patient.id)
            startActivity(intent)
        }
        binding.infantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ImmunizationHubActivity)
            adapter = infantAdapter
        }
    }

    private fun loadInfants() {
        lifecycleScope.launch {
            val patientDao = AppDatabase.getInstance(applicationContext).patientDao()
            // Fetch all patients from the database
            val allPatients = patientDao.getAllPatients()

            // Filter for infants (e.g., under 5 years old)
            val fiveYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -5) }.timeInMillis
            val infants = allPatients.filter { it.dob > fiveYearsAgo }

            infantAdapter.submitList(infants)
        }
    }
}
