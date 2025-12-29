package com.example.mediconnect_ai.utils

import java.util.Calendar

object AgeUtils {

    fun getFullAge(dobTimestamp: Long): String {
        val dob = Calendar.getInstance().apply { timeInMillis = dobTimestamp }
        val today = Calendar.getInstance()

        var years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            dob.add(Calendar.MONTH, 1)
            days += dob.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years--
            months += 12
        }

        return buildString {
            if (years > 0) append("$years year${if (years > 1) "s" else ""} ")
            if (months > 0) append("$months month${if (months > 1) "s" else ""} ")
            if (days > 0) append("$days day${if (days > 1) "s" else ""}")
        }.trim().ifEmpty { "0 days" }
    }

    fun getYears(dobTimestamp: Long): Int {
        val dob = Calendar.getInstance().apply { timeInMillis = dobTimestamp }
        val today = Calendar.getInstance()

        var years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            years--
        }
        return years
    }
}
