package com.example.mediconnect_ai.utils

import com.example.mediconnect_ai.database.PNCVisit
import java.util.Calendar

object PNCUtils {

    // Generate the standard 7 PNC visits based on Delivery Date
    fun generatePNCVisits(patientId: Long, deliveryDate: Long): List<PNCVisit> {
        val visits = mutableListOf<PNCVisit>()
        val calendar = Calendar.getInstance()

        fun getDateForDay(day: Int): Long {
            calendar.timeInMillis = deliveryDate
            calendar.add(Calendar.DAY_OF_YEAR, day)
            return calendar.timeInMillis
        }

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 1 (Within 24hrs)",
                dayNumber = 1,
                dueDate = getDateForDay(1)
            )
        )

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 3 Visit",
                dayNumber = 3,
                dueDate = getDateForDay(3)
            )
        )

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 7 Visit",
                dayNumber = 7,
                dueDate = getDateForDay(7)
            )
        )

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 14 Visit",
                dayNumber = 14,
                dueDate = getDateForDay(14)
            )
        )

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 21 Visit",
                dayNumber = 21,
                dueDate = getDateForDay(21)
            )
        )

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 28 Visit",
                dayNumber = 28,
                dueDate = getDateForDay(28)
            )
        )

        visits.add(
            PNCVisit(
                patientId = patientId,
                visitName = "PNC Day 42 (Final Check)",
                dayNumber = 42,
                dueDate = getDateForDay(42)
            )
        )

        return visits
    }
}
