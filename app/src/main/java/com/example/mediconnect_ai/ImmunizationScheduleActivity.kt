package com.example.mediconnect_ai

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapter.VaccineAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.VaccineStatus
import com.example.mediconnect_ai.databinding.ActivityImmunizationScheduleBinding
import kotlinx.coroutines.launch

class ImmunizationScheduleActivity : BaseActivity(), VaccineAdapter.OnVaccineStatusUpdateListener {

    private lateinit var binding: ActivityImmunizationScheduleBinding
    private lateinit var vaccineAdapter: VaccineAdapter
    private var patientId: Long = -1L

    companion object {
        const val EXTRA_DOB = "EXTRA_DOB"
        const val EXTRA_PATIENT_ID = "EXTRA_PATIENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImmunizationScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Immunization Schedule"
        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)

        setupRecyclerView()

        if (patientId != -1L) {
            // UPDATED: We now observe the database instead of loading once.
            observeScheduleFromDatabase(patientId)
        } else {
            Toast.makeText(this, "Could not load schedule. Patient ID is missing.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        vaccineAdapter = VaccineAdapter(mutableListOf(), this)
        binding.vaccineRecyclerView.apply {
            adapter = vaccineAdapter
            layoutManager = LinearLayoutManager(this@ImmunizationScheduleActivity)
        }
    }

    // UPDATED: This function is renamed and now uses observe()
    private fun observeScheduleFromDatabase(patientId: Long) {
        val vaccineDao = AppDatabase.getInstance(applicationContext).vaccineStatusDao()

        // This is the core of the fix. .observe() subscribes to the data.
        // The code inside the { ... } will automatically run every time the
        // vaccine data for this patient changes in the database.
        vaccineDao.getScheduleForPatient(patientId).observe(this) { vaccineStatusList ->
            // The LiveData gives us the updated list here.
            if (vaccineStatusList != null) {
                vaccineAdapter.updateVaccineList(vaccineStatusList)
            }
        }
    }

    override fun onUpdateStatus(vaccineStatus: VaccineStatus) {
        lifecycleScope.launch {
            val updatedVaccine = vaccineStatus.copy(status = "Completed")
            AppDatabase.getInstance(applicationContext).vaccineStatusDao().update(updatedVaccine)

            // NOTE: We no longer need to manually call loadScheduleFromDatabase().
            // The LiveData observer will automatically detect the change and update the UI.
            Toast.makeText(this@ImmunizationScheduleActivity, "${vaccineStatus.vaccineName} marked as completed.", Toast.LENGTH_SHORT).show()
        }
    }
}

