package com.example.mediconnect_ai

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.TaskListAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityPatientTasksBinding
import kotlinx.coroutines.launch

class PatientTasksActivity : BaseActivity() {

    private lateinit var binding: ActivityPatientTasksBinding
    private var patientId: Long = -1L

    companion object {
        const val EXTRA_PATIENT_ID = "EXTRA_PATIENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)

        if (patientId == -1L) {
            // Handle error, patient ID is essential
            finish()
            return
        }

        setupRecyclerView()
        loadPatientTasks()
    }

    private fun setupRecyclerView() {
        // We can reuse the same TaskListAdapter from the Daily Tasks screen
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadPatientTasks() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            // First, get the patient's name to set as the title
            val patient = db.patientDao().getPatientById(patientId)
            supportActionBar?.title = "Tasks for ${patient?.fullName}"

            // Then, fetch all tasks associated with this patient's ID
            val tasks = db.taskDao().getTasksForPatient(patientId)

            if (tasks.isEmpty()) {
                binding.tasksRecyclerView.visibility = View.GONE
                binding.tvNoTasks.visibility = View.VISIBLE
            } else {
                binding.tasksRecyclerView.visibility = View.VISIBLE
                binding.tvNoTasks.visibility = View.GONE
                // The TaskListAdapter needs a lambda for the checkbox click,
                // but we might not need to do anything here, so we pass an empty one.
                binding.tasksRecyclerView.adapter = TaskListAdapter(tasks) { /* No action on click */ }
            }
        }
    }
}
