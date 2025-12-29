package com.example.mediconnect_ai

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.TBProfile
import com.example.mediconnect_ai.database.TBVisit
import com.example.mediconnect_ai.databinding.ActivityTbScheduleBinding
import kotlinx.coroutines.launch

class TBScheduleActivity : BaseActivity() {

    private lateinit var binding: ActivityTbScheduleBinding
    private var patientId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTbScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patientId = intent.getLongExtra("PATIENT_ID", -1L)
        if (patientId == -1L) finish()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        loadVisits()
    }

    private fun loadVisits() {
        val db = AppDatabase.getInstance(applicationContext)

        lifecycleScope.launch {
            val tbProfile = db.tbDao().getActiveTBProfile(patientId)
            if (tbProfile == null) {
                Toast.makeText(this@TBScheduleActivity, "No TB record found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            val visits = db.tbDao().getVisitsForProfile(tbProfile.id)
            binding.recyclerView.adapter =
                TBVisitAdapter(visits) { visit ->
                    if (!visit.isCompleted) {
                        showVisitChecklistDialog(tbProfile, visit)
                    }
                }
        }
    }

    // ---------------- VISIT CHECKLIST ----------------

    private fun showVisitChecklistDialog(
        tbProfile: TBProfile,
        visit: TBVisit
    ) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_tb_visit_checklist, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupMedicines)
        val rbYes = view.findViewById<RadioButton>(R.id.rbYes)
        val cbSideEffects = view.findViewById<CheckBox>(R.id.cbSideEffects)

        AlertDialog.Builder(this)
            .setTitle("TB Follow-up Checklist")
            .setView(view)
            .setPositiveButton("Mark Done") { _, _ ->

                if (radioGroup.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "Please answer medicine intake", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val medicinesTaken = rbYes.isChecked
                val sideEffects = cbSideEffects.isChecked
                val needsReferral = !medicinesTaken || sideEffects

                updateVisitAndProfile(
                    tbProfile,
                    visit,
                    medicinesTaken,
                    sideEffects,
                    needsReferral
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- UPDATE VISIT + PROFILE ----------------

    private fun updateVisitAndProfile(
        tbProfile: TBProfile,
        visit: TBVisit,
        medicinesTaken: Boolean,
        sideEffects: Boolean,
        needsReferral: Boolean
    ) {
        val db = AppDatabase.getInstance(applicationContext)

        lifecycleScope.launch {
            // Update visit
            db.tbDao().updateVisit(
                visit.copy(
                    isCompleted = true,
                    completionDate = System.currentTimeMillis(),
                    medicinesTaken = medicinesTaken,
                    sideEffects = sideEffects,
                    needsReferral = needsReferral
                )
            )

            // 🔴 Update TB PROFILE risk flag
            if (needsReferral) {
                db.tbDao().updateProfile(
                    tbProfile.copy(
                        adherenceRisk = true,
                        lastFollowUpDate = System.currentTimeMillis()
                    )
                )
            }

            Toast.makeText(
                this@TBScheduleActivity,
                if (needsReferral)
                    "⚠ High-risk TB patient. Referral needed."
                else
                    "Visit marked completed",
                Toast.LENGTH_LONG
            ).show()

            loadVisits()
        }
    }
}
