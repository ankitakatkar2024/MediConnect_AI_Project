package com.example.mediconnect_ai

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.PatientListAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.PatientDao
import com.example.mediconnect_ai.databinding.ActivityRecordsBinding
import kotlinx.coroutines.launch

class RecordsActivity : BaseActivity() {

    private lateinit var binding: ActivityRecordsBinding
    private lateinit var patientDao: PatientDao

    // 1. Declare the adapter as a property so we can reuse it
    private lateinit var adapter: PatientListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientDao = AppDatabase.getInstance(applicationContext).patientDao()

        setupRecyclerView()
        loadPatients()
    }

    private fun setupRecyclerView() {
        // 2. Initialize Adapter with EMPTY constructor (Fixes "Too many arguments" error)
        adapter = PatientListAdapter()

        binding.recyclerViewPatients.apply {
            layoutManager = LinearLayoutManager(this@RecordsActivity)
            // Assign the adapter instance here once
            this.adapter = this@RecordsActivity.adapter
        }
    }

    private fun loadPatients() {
        // 3. Use the NEW grouped query logic
        lifecycleScope.launch {
            // Make sure getAllPatientsGroupedByFamily() is defined in your PatientDao
            patientDao.getAllPatientsGroupedByFamily().collect { patientList ->

                if (patientList.isEmpty()) {
                    // Handle empty state
                    binding.recyclerViewPatients.visibility = View.GONE
                    binding.tvNoRecords.visibility = View.VISIBLE
                } else {
                    // Show data
                    binding.recyclerViewPatients.visibility = View.VISIBLE
                    binding.tvNoRecords.visibility = View.GONE

                    // 4. Pass data to adapter using submitList()
                    adapter.submitList(patientList)
                }
            }
        }
    }
}