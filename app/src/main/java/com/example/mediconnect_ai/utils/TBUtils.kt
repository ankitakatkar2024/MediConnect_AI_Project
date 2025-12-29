package com.example.mediconnect_ai.utils

import com.example.mediconnect_ai.database.TBVisit
import java.util.Calendar

object TBUtils {

    private const val INTENSIVE_PHASE_WEEKS = 8    // Month 1–2
    private const val TOTAL_TREATMENT_WEEKS = 24   // 6 months total

    fun generateTreatmentSchedule(
        tbProfileId: Long,
        treatmentStartDate: Long
    ): List<TBVisit> {

        val visits = mutableListOf<TBVisit>()
        val calendar = Calendar.getInstance()

        fun calculateDate(daysAfterStart: Int): Long {
            calendar.timeInMillis = treatmentStartDate
            calendar.add(Calendar.DAY_OF_YEAR, daysAfterStart)
            return calendar.timeInMillis
        }

        // ================= INTENSIVE PHASE =================
        // Weekly visits (Month 1 & 2)
        for (week in 1..INTENSIVE_PHASE_WEEKS) {

            val month = if (week <= 4) 1 else 2

            val description = when (month) {

                1 -> """
                    Month 1 – Treatment Initiation (Intensive Phase)

                    Why this visit matters:
                    • Ensure TB medicines are started correctly and taken daily
                    • Check for early side effects (nausea, vomiting, jaundice)
                    • Educate patient on not missing doses
                    • Assess cough, fever, appetite, and weight

                    ASHA focus:
                    👉 Maximum counselling and daily adherence support
                """.trimIndent()

                else -> """
                    Month 2 – End of Intensive Phase

                    Why this visit matters:
                    • Confirm completion of Intensive Phase
                    • Check if TB symptoms are reducing
                    • Identify missed doses early
                    • Decide readiness for Continuation Phase

                    ASHA focus:
                    👉 Prevent early default and motivate continuation
                """.trimIndent()
            }

            visits.add(
                TBVisit(
                    tbProfileId = tbProfileId,
                    visitType = "Weekly Follow-up",
                    treatmentPhase = "IP",
                    dueDate = calculateDate(week * 7),
                    description = description
                )
            )
        }

        // ================= CONTINUATION PHASE =================
        // Bi-weekly visits (Month 3–6)
        var week = INTENSIVE_PHASE_WEEKS + 2

        while (week <= TOTAL_TREATMENT_WEEKS) {

            val month = when {
                week <= 12 -> 3
                week <= 16 -> 4
                week <= 20 -> 5
                else -> 6
            }

            val description = when (month) {

                3 -> """
                    Month 3 – Continuation Phase Start

                    Why this visit matters:
                    • Monitor transition to Continuation Phase
                    • Ensure medicines are taken without gaps
                    • Check for treatment fatigue
                    • Monitor weight gain and symptom improvement

                    ASHA focus:
                    👉 “TB looks better, but treatment must continue”
                """.trimIndent()

                4 -> """
                    Month 4 – Adherence Reinforcement

                    Why this visit matters:
                    • Identify patients becoming irregular
                    • Monitor long-term medicine side effects
                    • Track nutrition and weight recovery
                    • Watch for warning signs of relapse

                    ASHA focus:
                    👉 Home visits and family counselling
                """.trimIndent()

                5 -> """
                    Month 5 – Pre-Completion Monitoring

                    Why this visit matters:
                    • Ensure patient does not stop treatment early
                    • Check if doses were skipped
                    • Identify need for referral if symptoms persist
                    • Prepare patient mentally for completion

                    ASHA focus:
                    👉 Prevent last-month dropouts
                """.trimIndent()

                else -> """
                    Month 6 – Treatment Completion

                    Why this visit matters:
                    • Confirm full treatment completion
                    • Record final health status
                    • Check for residual symptoms
                    • Counsel on relapse warning signs
                    • Guide on follow-up and nutrition

                    ASHA focus:
                    👉 Close treatment formally and ensure documentation
                """.trimIndent()
            }

            visits.add(
                TBVisit(
                    tbProfileId = tbProfileId,
                    visitType = "Bi-weekly Follow-up",
                    treatmentPhase = "CP",
                    dueDate = calculateDate(week * 7),
                    description = description
                )
            )

            week += 2
        }

        return visits
    }
}
