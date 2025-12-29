package com.example.mediconnect_ai

import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import android.preference.PreferenceManager
import java.util.Locale

class OutbreakMapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var tvStatus: TextView
    private val db = FirebaseFirestore.getInstance()

    // Data class for our symptom points
    data class SymptomLog(
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val symptom: String = "",
        val timestamp: Long = 0L
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init OSMDroid
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        setContentView(R.layout.activity_outbreak_map)

        map = findViewById(R.id.mapOutbreak)
        tvStatus = findViewById(R.id.tvRiskStatus)

        // Setup the "Help" button for the AI Slide
        val fabHelp = findViewById<FloatingActionButton>(R.id.fabAiHelp)
        fabHelp.setOnClickListener {
            showAiExplanationSlide()
        }

        setupMap()
        fetchAndAnalyzeData()
    }

    private fun setupMap() {
        map.setMultiTouchControls(true)
        map.controller.setZoom(14.0)
        // Default center (e.g., Ghansoli)
        map.controller.setCenter(GeoPoint(19.10, 72.99))
    }

    private fun fetchAndAnalyzeData() {
        // 1. Get data from last 7 days
        val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)

        db.collection("symptom_logs")
            .whereGreaterThan("timestamp", oneWeekAgo)
            .get()
            .addOnSuccessListener { documents ->
                val logs = documents.toObjects(SymptomLog::class.java)
                runOutbreakDetection(logs)
            }
            .addOnFailureListener {
                tvStatus.text = "Error fetching data."
            }
    }

    // --- UPDATED AI LOGIC: Detects Specific Disease Type & Location Name ---
    private fun runOutbreakDetection(logs: List<SymptomLog>) {
        if (logs.isEmpty()) {
            tvStatus.text = "No recent data found. Status: LOW RISK (Green)"
            tvStatus.setTextColor(Color.parseColor("#388E3C")) // Dark Green
            return
        }

        val clusters = mutableListOf<MutableList<SymptomLog>>()

        // 1. Cluster Logic: Group points within 500 meters
        for (log in logs) {
            var addedToCluster = false
            for (cluster in clusters) {
                // Check distance to the first item in the cluster
                val center = cluster[0]
                val distance = calculateDistance(log.lat, log.lng, center.lat, center.lng)

                if (distance < 500) { // 500 meters radius
                    cluster.add(log)
                    addedToCluster = true
                    break
                }
            }
            if (!addedToCluster) {
                clusters.add(mutableListOf(log))
            }
        }

        // 2. Analyze Clusters for DOMINANT Disease & Location
        var maxRiskLevel = 0

        for (cluster in clusters) {
            val caseCount = cluster.size
            val centerLog = cluster[0] // We use the first case to identify the location

            // Get Real Address Name (e.g., "Ghansoli", "Sector 5")
            val locationName = getAddressFromLocation(centerLog.lat, centerLog.lng)

            // Find the most common symptom
            var dominantSymptom = cluster.groupingBy { it.symptom }
                .eachCount()
                .maxByOrNull { it.value }?.key ?: "Viral"

            // Check risk logic
            val isSevere = dominantSymptom.contains("Dengue", true) ||
                    dominantSymptom.contains("High Fever", true)

            // Clean up text
            if (dominantSymptom.contains("Dengue")) dominantSymptom = "Dengue"
            if (dominantSymptom.contains("Fever")) dominantSymptom = "Viral Fever"

            if (caseCount >= 5 || (caseCount >= 3 && isSevere)) {
                // RED ZONE
                drawRiskZone(centerLog.lat, centerLog.lng, "RED", dominantSymptom, locationName)
                maxRiskLevel = 2
            } else if (caseCount >= 3) {
                // YELLOW ZONE
                drawRiskZone(centerLog.lat, centerLog.lng, "YELLOW", dominantSymptom, locationName)
                if (maxRiskLevel < 1) maxRiskLevel = 1
            }
        }

        // Update Status Text if no risks found
        if (maxRiskLevel == 0) {
            tvStatus.text = "Status Normal: No clustering detected."
            tvStatus.setTextColor(Color.parseColor("#388E3C"))
        }
    }

    // --- UPDATED DRAW FUNCTION: Accepts Location Name ---
    private fun drawRiskZone(
        lat: Double,
        lng: Double,
        riskLevel: String,
        diseaseType: String,
        locationName: String
    ) {
        val circle = Polygon()
        circle.points = Polygon.pointsAsCircle(GeoPoint(lat, lng), 500.0) // 500m radius

        val fillColor: Int
        val strokeColor: Int
        val alertTitle: String

        if (riskLevel == "RED") {
            fillColor = Color.argb(70, 255, 0, 0) // Translucent Red
            strokeColor = Color.RED
            alertTitle = "CRITICAL: $diseaseType in $locationName"

            // Update Top Alert Banner
            tvStatus.text = "⚠️ ALERT: Active $diseaseType Cluster in $locationName!"
            tvStatus.setTextColor(Color.RED)

        } else {
            fillColor = Color.argb(70, 255, 235, 59) // Translucent Yellow
            strokeColor = Color.parseColor("#FBC02D") // Darker Yellow border
            alertTitle = "Warning: Rising $diseaseType in $locationName"

            if (tvStatus.currentTextColor != Color.RED) {
                tvStatus.text = "⚠️ Warning: Potential $diseaseType in $locationName"
                tvStatus.setTextColor(Color.parseColor("#FFA000"))
            }
        }

        circle.fillPaint.color = fillColor
        circle.outlinePaint.color = strokeColor
        circle.outlinePaint.strokeWidth = 3.0f
        circle.title = alertTitle
        map.overlays.add(circle)

        // Add Marker with Location Name
        val marker = Marker(map)
        marker.position = GeoPoint(lat, lng)
        marker.title = "$diseaseType Cluster ($riskLevel)"
        marker.snippet = "Location: $locationName"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        map.overlays.add(marker)

        // Force Map to Look at this location
        map.controller.setCenter(GeoPoint(lat, lng))
        map.controller.setZoom(16.0)
        map.invalidate()
    }

    // --- NEW HELPER: Reverse Geocoding (Gets Address Name) ---
    private fun getAddressFromLocation(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                // Try to get the most specific name available
                address.subLocality ?: address.locality ?: address.subAdminArea ?: "Unknown Area"
            } else {
                "Unknown Location"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Map Location"
        }
    }

    // Helper: Distance in meters
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val loc1 = Location("").apply { latitude = lat1; longitude = lon1 }
        val loc2 = Location("").apply { latitude = lat2; longitude = lon2 }
        return loc1.distanceTo(loc2)
    }

    // --- AI EXPLANATION SLIDE (Dialog) ---
    private fun showAiExplanationSlide() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_ai_explanation, null)
        builder.setView(view)
        builder.setPositiveButton("Got it", null)
        builder.show()
    }
}