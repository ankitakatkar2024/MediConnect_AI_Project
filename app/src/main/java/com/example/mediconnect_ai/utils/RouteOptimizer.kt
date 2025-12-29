package com.example.mediconnect_ai.utils

import android.location.Location
import com.example.mediconnect_ai.database.Patient

object RouteOptimizer {

    /**
     * Sorts a list of patients to create the shortest travel path.
     * Uses a "Nearest Neighbor" algorithm.
     */
    fun optimizeRoute(startLat: Double, startLng: Double, patients: List<Patient>): List<Patient> {
        val optimizedList = mutableListOf<Patient>()
        val remainingPatients = patients.toMutableList()

        // Start from the ASHA worker's current location
        var currentLocation = Location("current").apply {
            latitude = startLat
            longitude = startLng
        }

        while (remainingPatients.isNotEmpty()) {
            // Find the patient closest to the CURRENT location
            val nearest = remainingPatients.minByOrNull { patient ->
                val targetLoc = Location("target").apply {
                    latitude = patient.latitude
                    longitude = patient.longitude
                }
                currentLocation.distanceTo(targetLoc)
            }

            nearest?.let {
                // Add them to the route
                optimizedList.add(it)
                // Remove them from the "to-do" list
                remainingPatients.remove(it)

                // Move our virtual "current location" to this patient
                // (So the next loop finds who is closest to THIS patient)
                currentLocation = Location("next").apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
        }
        return optimizedList
    }
}