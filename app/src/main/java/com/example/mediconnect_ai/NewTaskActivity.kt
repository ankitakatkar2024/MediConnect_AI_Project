package com.example.mediconnect_ai

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.database.Task
import com.example.mediconnect_ai.databinding.ActivityNewTaskBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewTaskActivity : BaseActivity() {

    private lateinit var binding: ActivityNewTaskBinding
    private var patientList: List<Patient> = emptyList()
    private var selectedTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPatientsIntoSpinner()

        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSaveTask.setOnClickListener { saveTask() }
    }

    private fun loadPatientsIntoSpinner() {
        val patientDao = AppDatabase.getInstance(applicationContext).patientDao()
        lifecycleScope.launch {
            patientList = patientDao.getAllPatients()
            val patientNames = patientList.map { it.fullName }
            val adapter = ArrayAdapter(this@NewTaskActivity, android.R.layout.simple_spinner_item, patientNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerPatients.adapter = adapter
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                selectedTimestamp = selectedDate.timeInMillis
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                binding.tvSelectedDate.text = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTask() {
        val selectedPatientPosition = binding.spinnerPatients.selectedItemPosition
        val description = binding.etTaskDescription.text.toString().trim()

        if (patientList.isEmpty()) {
            Toast.makeText(this, "Please register a patient first", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            binding.etTaskDescription.error = "Description cannot be empty"
            return
        }
        if (selectedTimestamp == 0L) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPatient = patientList[selectedPatientPosition]
        val task = Task(
            patientId = selectedPatient.id,
            patientName = selectedPatient.fullName,
            taskDescription = description,
            dueDate = selectedTimestamp
        )

        val taskDao = AppDatabase.getInstance(applicationContext).taskDao()
        lifecycleScope.launch {
            taskDao.insert(task)
            Toast.makeText(this@NewTaskActivity, "Task saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}