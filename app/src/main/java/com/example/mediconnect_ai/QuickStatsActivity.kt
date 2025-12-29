package com.example.mediconnect_ai

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityQuickStatsBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class QuickStatsActivity : BaseActivity() {

    private lateinit var binding: ActivityQuickStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuickStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadStats()
    }

    private fun loadStats() {
        val db = AppDatabase.getInstance(applicationContext)
        val patientDao = db.patientDao()
        val taskDao = db.taskDao() // MODIFIED: Use TaskDao

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfToday = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfToday = calendar.timeInMillis

        lifecycleScope.launch {
            val totalPatients = patientDao.getTotalPatientCount()
            val todayRegistrations = patientDao.getPatientsRegisteredTodayCount(startOfToday, endOfToday)
            // MODIFIED: Fetch today's tasks count
            val todayTasks = taskDao.getIncompleteTasksForTodayCount(startOfToday, endOfToday)

            // Update the UI with the fetched data
            binding.tvTotalPatientsCount.text = totalPatients.toString()
            binding.tvTodayRegistrationsCount.text = todayRegistrations.toString()
            // MODIFIED: Update the new TextView ID
            binding.tvTodayTasksCount.text = todayTasks.toString()
        }
    }
}