package com.example.mediconnect_ai

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.TBProfile
import com.example.mediconnect_ai.databinding.ActivityRegisterTbBinding
import com.example.mediconnect_ai.utils.TBUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterTBActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterTbBinding
    private var patientId: Long = -1L
    private var treatmentStartDate: Long = 0L

    companion object {
        const val EXTRA_PATIENT_ID = "PATIENT_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterTbBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)
        if (patientId == -1L) {
            finish()
            return
        }

        setupSpinners()

        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSaveTB.setOnClickListener { saveTBRecord() }
    }

    private fun setupSpinners() {
        val types = arrayOf("Pulmonary", "Extra-Pulmonary")
        binding.spinnerTBType.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        val resistance = arrayOf("Drug Sensitive (DS)", "MDR", "XDR")
        binding.spinnerDrugResistance.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resistance)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                    treatmentStartDate = timeInMillis
                }
                binding.tvSelectedDate.text =
                    "Treatment Start: $day/${month + 1}/$year"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTBRecord() {
        if (treatmentStartDate == 0L) {
            Toast.makeText(this, "Please select treatment start date", Toast.LENGTH_SHORT).show()
            return
        }

        val tbType = binding.spinnerTBType.selectedItem.toString()
        val resistance = binding.spinnerDrugResistance.selectedItem.toString()
        val nikshayId = binding.etNikshayId.text.toString().ifBlank { null }

        val continuationPhaseStart =
            treatmentStartDate + (60L * 24 * 60 * 60 * 1000)

        val tbProfile = TBProfile(
            patientId = patientId,
            diagnosisDate = System.currentTimeMillis(), // ✅ ADD THIS
            nikshayId = nikshayId,
            tbType = tbType,
            drugResistanceType = when {
                resistance.contains("MDR") -> "MDR"
                resistance.contains("XDR") -> "XDR"
                else -> "DS"
            },
            treatmentStartDate = treatmentStartDate,
            continuationPhaseStartDate = continuationPhaseStart,
            treatmentDurationMonths = 6,
            treatmentPhase = "IP",
            lastFollowUpDate = null,     // ✅ FIX HERE
            adherenceRisk = false,
            isActive = true,
            outcome = null,
            dbtStatus = null
        )

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)

            val profileId = db.tbDao().insertProfile(tbProfile)

            val visits = TBUtils.generateTreatmentSchedule(
                tbProfileId = profileId,
                treatmentStartDate = treatmentStartDate
            )
            db.tbDao().insertVisits(visits)

            Toast.makeText(
                this@RegisterTBActivity,
                "TB record registered & follow-up schedule created",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
}
