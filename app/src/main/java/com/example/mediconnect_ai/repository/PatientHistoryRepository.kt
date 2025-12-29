package com.example.mediconnect_ai.repository

import android.content.Context
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.models.EventType
import com.example.mediconnect_ai.models.HistoryEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class PatientHistoryRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)

    suspend fun getFullPatientHistory(patientId: Long): List<HistoryEvent> =
        withContext(Dispatchers.IO) {

            val historyList = mutableListOf<HistoryEvent>()

            /* -------------------- 1. Registration -------------------- */
            val patient = db.patientDao().getPatientById(patientId)
            patient?.let {
                historyList.add(
                    HistoryEvent(
                        dateTimestamp = it.registrationTimestamp,
                        eventType = EventType.REGISTRATION,
                        title = "Patient Registered",
                        description = "Added to MediConnect System",
                        status = "Active"
                    )
                )
            }

            /* -------------------- 2. Pregnancy + ANC -------------------- */
            val pregnancies = db.pregnancyDao().getAllPregnanciesForPatient(patientId)
            for (preg in pregnancies) {

                // Pregnancy start
                historyList.add(
                    HistoryEvent(
                        dateTimestamp = preg.lmpDate,
                        eventType = EventType.PREGNANCY_START,
                        title = "Pregnancy Detected",
                        description = "LMP: ${Date(preg.lmpDate)}",
                        status = if (preg.isActive) "Ongoing" else "Closed"
                    )
                )

                // Delivery event
                val deliveryDate = preg.deliveryDate
                if (!preg.isActive && deliveryDate != null && deliveryDate > 0) {
                    historyList.add(
                        HistoryEvent(
                            dateTimestamp = deliveryDate,
                            eventType = EventType.DELIVERY,
                            title = "Delivery Recorded",
                            description = "Baby Born",
                            status = "Completed"
                        )
                    )
                }

                // ANC visits
                val ancVisits = db.ancVisitDao().getVisitsForPregnancy(preg.id)
                for (visit in ancVisits) {
                    historyList.add(
                        HistoryEvent(
                            dateTimestamp = visit.dueDate,
                            eventType = EventType.ANC_VISIT,
                            title = "ANC Visit: ${visit.visitName}",
                            description = if (visit.isCompleted) "Visit Completed" else "Visit Pending",
                            status = if (visit.isCompleted) "Completed" else "Scheduled"
                        )
                    )
                }
            }

            /* -------------------- 3. PNC Visits -------------------- */
            val pncVisits = db.pncVisitDao().getPNCVisitsForPatient(patientId)
            for (visit in pncVisits) {
                historyList.add(
                    HistoryEvent(
                        dateTimestamp = visit.dueDate,
                        eventType = EventType.PNC_VISIT,
                        title = visit.visitName,
                        description = "Post-Natal Checkup",
                        status = if (visit.isCompleted) "Completed" else "Scheduled"
                    )
                )
            }

            /* -------------------- 4. Vaccines -------------------- */
            val vaccines = db.vaccineStatusDao().getScheduleForPatientAsList(patientId)
            for (vac in vaccines) {
                historyList.add(
                    HistoryEvent(
                        // ✅ FIX: use dueDate only (dateAdministered does NOT exist)
                        dateTimestamp = vac.dueDate,
                        eventType = EventType.VACCINE,
                        title = "Vaccine: ${vac.vaccineName}",
                        description = "Status: ${vac.status}",
                        status = vac.status
                    )
                )
            }

            /* -------------------- 5. NEW: TB (Tuberculosis) Records 🚨 -------------------- */
            // We check if a TB Profile exists for this patient
            val tbProfile = db.tbDao().getActiveTBProfile(patientId)

            if (tbProfile != null) {
                // A. Add the Start of Treatment Event
                historyList.add(
                    HistoryEvent(
                        dateTimestamp = tbProfile.diagnosisDate,
                        eventType = EventType.ALERT, // Using ALERT type for severe disease
                        title = "TB Treatment Started (NTEP)",
                        description = "Phase: ${tbProfile.treatmentPhase}",
                        status = "Ongoing"
                    )
                )

                // B. Add All Scheduled & Completed TB Visits (The Timeline View)
                // Note: Ensure your TBDao has a method `getVisitsForProfile(profileId)`
                val tbVisits = db.tbDao().getVisitsForProfile(tbProfile.id)

                for (visit in tbVisits) {
                    historyList.add(
                        HistoryEvent(
                            dateTimestamp = visit.dueDate,
                            eventType = EventType.ALERT, // Reusing ALERT or create a specific TB_VISIT type
                            title = "TB Visit: ${visit.visitType}",
                            description = "Phase: ${visit.treatmentPhase}",
                            // Logic: If completed -> Green "Completed", Else -> Grey "Scheduled"
                            status = if (visit.isCompleted) "Completed" else "Scheduled"
                        )
                    )
                }
            }

            // Sort by Date (Newest at the top)
            return@withContext historyList.sortedByDescending { it.dateTimestamp }
        }
}