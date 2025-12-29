package com.example.mediconnect_ai

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.database.ANCVisit
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityAncScheduleBinding
import com.example.mediconnect_ai.firestore.FirebaseANCVisitHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ANCScheduleActivity : BaseActivity() {

    private lateinit var binding: ActivityAncScheduleBinding
    private lateinit var adapter: ANCVisitAdapter
    private var patientId: Long = -1L

    companion object {
        const val EXTRA_PATIENT_ID = "PATIENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAncScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)
        if (patientId == -1L) {
            Toast.makeText(this, "Error: Patient ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = ANCVisitAdapter { visit ->
            markVisitAsCompleted(visit)
        }
        binding.recyclerViewVisits.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewVisits.adapter = adapter
    }

    private fun loadData() {
        val db = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            val pregnancy = db.pregnancyDao().getActivePregnancyForPatient(patientId)

            if (pregnancy != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.tvPatientName.text = pregnancy.patientName
                binding.tvLmpDate.text = "LMP: ${sdf.format(Date(pregnancy.lmpDate))}"
                binding.tvEddDate.text = "EDD: ${sdf.format(Date(pregnancy.edd))}"

                val visits = db.ancVisitDao().getVisitsForPregnancy(pregnancy.id)
                adapter.submitList(visits)
            } else {
                Toast.makeText(this@ANCScheduleActivity, "No active pregnancy found.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun markVisitAsCompleted(visit: ANCVisit) {
        val db = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            val updatedVisit = visit.copy(
                isCompleted = true,
                completionDate = System.currentTimeMillis()
            )

            // 1. Update locally
            db.ancVisitDao().update(updatedVisit)

            // 2. Sync to Firestore (pregnancyId is part of ANCVisit)
            FirebaseANCVisitHelper.saveVisit(updatedVisit, visit.pregnancyId) { success, _ ->
                if (success) {
                    // optional: show different message
                }
            }

            loadData()
            Toast.makeText(this@ANCScheduleActivity, "Visit marked as completed!", Toast.LENGTH_SHORT).show()
        }
    }
}
