package com.example.mediconnect_ai

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.HistoryAdapter
import com.example.mediconnect_ai.databinding.ActivityPatientHistoryBinding
import com.example.mediconnect_ai.repository.PatientHistoryRepository
import kotlinx.coroutines.launch

class PatientHistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientHistoryBinding
    private lateinit var repository: PatientHistoryRepository

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)
        if (patientId == -1L) {
            Toast.makeText(this, "Error: Invalid Patient ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        repository = PatientHistoryRepository(this)

        loadHistory(patientId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarHistory)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarHistory.setNavigationOnClickListener { finish() }
    }

    private fun loadHistory(patientId: Long) {
        lifecycleScope.launch {
            try {
                // 1. Get Aggregated Data
                val historyList = repository.getFullPatientHistory(patientId)

                // 2. Setup RecyclerView
                binding.recyclerHistory.layoutManager = LinearLayoutManager(this@PatientHistoryActivity)
                binding.recyclerHistory.adapter = HistoryAdapter(historyList)

            } catch (e: Exception) {
                Toast.makeText(this@PatientHistoryActivity, "Failed to load history: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }
}