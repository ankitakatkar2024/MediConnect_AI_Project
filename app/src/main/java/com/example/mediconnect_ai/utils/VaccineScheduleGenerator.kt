package com.example.mediconnect_ai.utils

import com.example.mediconnect_ai.model.Vaccine // <-- ERROR FIXED HERE
import java.util.Calendar
import java.util.Date

/**
 * A singleton object responsible for generating a personalized immunization schedule.
 * It contains all the business logic based on the National Immunization Schedule.
 */
object VaccineScheduleGenerator {

    // A private helper data class to cleanly store the rules of the schedule.
    // This is only used inside this object and is not visible to the rest of the app.
    private data class ScheduleRule(val name: String, val importance: String, val calendarField: Int, val amount: Int)

    // The complete list of vaccination rules based on the schedule you provided.
    // Each rule specifies how to calculate the due date from the date of birth.
    private val scheduleRules = listOf(
        // For Infants
        ScheduleRule("BCG", "Prevents tuberculosis.", Calendar.DATE, 0),
        ScheduleRule("Hepatitis B (Birth dose)", "Fights liver infections.", Calendar.HOUR_OF_DAY, 24),
        ScheduleRule("OPV-0", "Protects against polio.", Calendar.DATE, 15),
        ScheduleRule("OPV 1", "Protects against polio.", Calendar.WEEK_OF_YEAR, 6),
        ScheduleRule("Pentavalent 1", "Protects against diphtheria, pertussis, tetanus, polio, hepatitis B, pneumonia, and diarrheal diseases.", Calendar.WEEK_OF_YEAR, 6),
        ScheduleRule("Rotavirus 1", "Protects against rotavirus diarrhea.", Calendar.WEEK_OF_YEAR, 6),
        ScheduleRule("IPV 1", "Reinforces protection against polio.", Calendar.WEEK_OF_YEAR, 6),
        ScheduleRule("OPV 2", "Second dose for polio.", Calendar.WEEK_OF_YEAR, 10),
        ScheduleRule("Pentavalent 2", "Second combination dose.", Calendar.WEEK_OF_YEAR, 10),
        ScheduleRule("Rotavirus 2", "Second dose for rotavirus.", Calendar.WEEK_OF_YEAR, 10),
        ScheduleRule("OPV 3", "Third dose gives sustained protection.", Calendar.WEEK_OF_YEAR, 14),
        ScheduleRule("Pentavalent 3", "Third combination dose.", Calendar.WEEK_OF_YEAR, 14),
        ScheduleRule("Rotavirus 3", "Third dose for rotavirus.", Calendar.WEEK_OF_YEAR, 14),
        ScheduleRule("IPV 2", "Second dose for polio reinforcement.", Calendar.WEEK_OF_YEAR, 14),
        ScheduleRule("Measles/MR 1st Dose", "Measles can be fatal in infants; rubella causes birth defects.", Calendar.MONTH, 9),
        ScheduleRule("JE-1", "Protects against Japanese Encephalitis in endemic areas.", Calendar.MONTH, 9),
        ScheduleRule("Vitamin A (1st dose)", "Prevents vitamin A deficiency and reduces morbidity.", Calendar.MONTH, 9),

        // For Children
        ScheduleRule("DPT booster-1", "Important booster to prolong childhood protection.", Calendar.MONTH, 16),
        ScheduleRule("Measles/MR 2nd dose", "Combats measles, mumps, rubella, and provides long-term immunity.", Calendar.MONTH, 16),
        ScheduleRule("OPV Booster", "Crucial booster dose to maintain polio immunity.", Calendar.MONTH, 16),
        ScheduleRule("JE-2", "Second dose for long-term Japanese Encephalitis protection.", Calendar.MONTH, 16),
        ScheduleRule("Vitamin A (2nd dose)", "Biannual dose to prevent deficiency and boost immunity.", Calendar.MONTH, 18),
        ScheduleRule("DPT Booster-2", "Prepares children for school environment exposure.", Calendar.YEAR, 5),
        ScheduleRule("TT", "Immunity reinforcement at puberty and before adulthood.", Calendar.YEAR, 10),
        ScheduleRule("TT (2nd)", "Last adolescent booster before adulthood.", Calendar.YEAR, 16)
    )

    /**
     * Generates a chronological list of vaccines for a given date of birth.
     * @param dateOfBirth The infant's date of birth.
     * @return A list of [Vaccine] objects, sorted by their due date.
     */
    fun generateSchedule(dateOfBirth: Date): List<Vaccine> {
        val schedule = mutableListOf<Vaccine>()
        val dobCalendar = Calendar.getInstance().apply { time = dateOfBirth }

        // Iterate through each rule to calculate the due date.
        for (rule in scheduleRules) {
            // Create a calendar instance and set it to the infant's date of birth for each calculation.
            val dueDateCalendar = Calendar.getInstance().apply { time = dobCalendar.time }

            // Add the specified amount of time (e.g., 6 weeks) to the date of birth.
            dueDateCalendar.add(rule.calendarField, rule.amount)

            // Create a new Vaccine object with the calculated due date and add it to our schedule list.
            schedule.add(Vaccine(rule.name, rule.importance, dueDateCalendar.time))
        }
        // Return the final list, sorted chronologically.
        return schedule.sortedBy { it.dueDate }
    }
}

