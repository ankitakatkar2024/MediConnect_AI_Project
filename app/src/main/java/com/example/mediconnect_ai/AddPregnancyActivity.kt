package com.example.mediconnect_ai

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.ANCVisit
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Pregnancy
import com.example.mediconnect_ai.databinding.ActivityAddPregnancyBinding
import com.example.mediconnect_ai.firestore.FirebaseANCVisitHelper
import com.example.mediconnect_ai.firestore.FirebasePregnancyHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPregnancyActivity : BaseActivity() {

    private lateinit var binding: ActivityAddPregnancyBinding
    private var patientId: Long = -1L
    private var patientName: String? = null
    private var lmpTimestamp: Long = 0L
    private var ashaId: String? = null

    companion object {
        const val EXTRA_PATIENT_ID = "PATIENT_ID"
        const val EXTRA_PATIENT_NAME = "PATIENT_NAME"
        const val EXTRA_ASHA_ID = "ASHA_ID"
        private const val TAG = "AddPregnancyActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPregnancyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)
        patientName = intent.getStringExtra(EXTRA_PATIENT_NAME)
        ashaId = intent.getStringExtra(EXTRA_ASHA_ID)

        if (patientId == -1L || patientName == null) {
            Toast.makeText(this, "Error: Patient data not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvPatientName.text = patientName

        binding.btnSelectLmpDate.setOnClickListener { showLmpDatePicker() }
        binding.btnSavePregnancy.setOnClickListener { savePregnancy() }
    }

    private fun showLmpDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                lmpTimestamp = selectedDate.timeInMillis

                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                binding.tvSelectedLmpDate.text = "LMP: ${dateFormat.format(selectedDate.time)}"

                calculateAndDisplayEdd(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun calculateAndDisplayEdd(lmpDate: Calendar) {
        val eddCalendar = lmpDate.clone() as Calendar
        eddCalendar.add(Calendar.DAY_OF_YEAR, 280)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.tvEdd.text = dateFormat.format(eddCalendar.time)
    }

    private fun savePregnancy() {
        if (lmpTimestamp == 0L) {
            Toast.makeText(this, "Please select the LMP date.", Toast.LENGTH_SHORT).show()
            return
        }

        val eddCalendar = Calendar.getInstance().apply { timeInMillis = lmpTimestamp }
        eddCalendar.add(Calendar.DAY_OF_YEAR, 280)
        val eddTimestamp = eddCalendar.timeInMillis

        val pregnancy = Pregnancy(
            patientId = patientId,
            patientName = patientName!!,
            lmpDate = lmpTimestamp,
            edd = eddTimestamp,
            ashaId = ashaId ?: ""
        )

        val db = AppDatabase.getInstance(applicationContext)
        lifecycleScope.launch {
            // 1. Insert pregnancy locally and get ID
            val newPregnancyId = db.pregnancyDao().insert(pregnancy)

            val pregnancyWithId = pregnancy.copy(id = newPregnancyId)

            // 2. Sync pregnancy to Firestore
            FirebasePregnancyHelper.savePregnancy(pregnancyWithId) { success, error ->
                if (success) {
                    Log.d(TAG, "Cloud sync OK for pregnancyId=$newPregnancyId")
                    Toast.makeText(
                        this@AddPregnancyActivity,
                        "✅ Synced to cloud (Pregnancy ID: $newPregnancyId)",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e(TAG, "Cloud sync FAILED for pregnancyId=$newPregnancyId: $error")
                    Toast.makeText(
                        this@AddPregnancyActivity,
                        "⚠️ Cloud sync failed, saved only on this phone.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // 3. Generate and insert & sync ANC visits
            scheduleAncTasks(newPregnancyId, lmpTimestamp)

            Toast.makeText(
                this@AddPregnancyActivity,
                "Pregnancy registered & ANC Schedule created!",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private suspend fun scheduleAncTasks(pregnancyId: Long, lmp: Long) {
        val db = AppDatabase.getInstance(applicationContext)
        val ancVisitDao = db.ancVisitDao()
        val lmpCalendar = Calendar.getInstance().apply { timeInMillis = lmp }

        val visits = mutableListOf<ANCVisit>()

        fun getFutureDate(weeks: Int): Long {
            val cal = lmpCalendar.clone() as Calendar
            cal.add(Calendar.WEEK_OF_YEAR, weeks)
            return cal.timeInMillis
        }

        visits.add(
            ANCVisit(
                pregnancyId = pregnancyId,
                visitName = "1st ANC Visit (Registration)",
                dueWeekStart = 0,
                dueWeekEnd = 12,
                dueDate = getFutureDate(12)
            )
        )

        visits.add(
            ANCVisit(
                pregnancyId = pregnancyId,
                visitName = "2nd ANC Visit & TT-1",
                dueWeekStart = 14,
                dueWeekEnd = 26,
                dueDate = getFutureDate(20)
            )
        )

        visits.add(
            ANCVisit(
                pregnancyId = pregnancyId,
                visitName = "3rd ANC Visit & TT-2",
                dueWeekStart = 28,
                dueWeekEnd = 34,
                dueDate = getFutureDate(32)
            )
        )

        visits.add(
            ANCVisit(
                pregnancyId = pregnancyId,
                visitName = "4th ANC Visit (Pre-delivery)",
                dueWeekStart = 36,
                dueWeekEnd = 40,
                dueDate = getFutureDate(36)
            )
        )

        // Insert all visits and get their IDs
        val ids = ancVisitDao.insertAll(visits)

        // Sync each visit to Firestore with its generated ID
        visits.forEachIndexed { index, visit ->
            val visitWithId = visit.copy(id = ids[index])
            FirebaseANCVisitHelper.saveVisit(visitWithId, pregnancyId) { _, _ -> }
        }
    }
}
