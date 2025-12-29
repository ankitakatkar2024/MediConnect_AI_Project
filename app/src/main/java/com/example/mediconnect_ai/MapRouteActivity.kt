package com.example.mediconnect_ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.utils.RouteOptimizer
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class MapRouteActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var tvRouteInfo: TextView
    private lateinit var btnMarkVisited: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0

    private var myLocationMarker: Marker? = null
    private var optimizedRoute: MutableList<Patient> = mutableListOf()
    private var visitedPatients: MutableList<Patient> = mutableListOf()

    private var isFirstLocationUpdate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setContentView(R.layout.activity_map_route)

        map = findViewById(R.id.map)
        // Software rendering helps with lines on emulators
        map.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        tvRouteInfo = findViewById(R.id.tvRouteInfo)
        btnMarkVisited = findViewById(R.id.btnMarkVisited)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnStartNav).setOnClickListener { startNavigation() }
        findViewById<FloatingActionButton>(R.id.btnRecalculate).setOnClickListener { refreshRoute() }

        btnMarkVisited.setOnClickListener { markCurrentTargetAsVisited() }

        setupMap()
        setupLocationUpdates()
    }

    private fun setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(18.0)
    }

    private fun setupLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Ignore (0,0) coordinates from emulator
                    if (location.latitude == 0.0 && location.longitude == 0.0) continue

                    currentLat = location.latitude
                    currentLng = location.longitude

                    updateMyLocationMarker(GeoPoint(currentLat, currentLng))

                    if (isFirstLocationUpdate) {
                        isFirstLocationUpdate = false
                        map.controller.animateTo(GeoPoint(currentLat, currentLng))
                        calculateRoute()
                    }
                }
            }
        }
    }

    private fun updateMyLocationMarker(point: GeoPoint) {
        if (myLocationMarker == null || !map.overlays.contains(myLocationMarker)) {
            myLocationMarker = Marker(map)
            myLocationMarker?.title = "You are Here (Nurse/Doctor)"
            myLocationMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // ✅ FIX: Use standard Android System Icon instead of broken library reference
            // This guarantees the code will compile and run.
            val icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation)?.mutate()

            // 🔴 Role Color: RED (Nurse/You)
            icon?.setTint(Color.RED)
            myLocationMarker?.icon = icon

            map.overlays.add(myLocationMarker)
        }
        myLocationMarker?.position = point
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        startLocationTracking()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        stopLocationTracking()
    }

    private fun startLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build(),
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Toast.makeText(this, "Location Permission Required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopLocationTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun calculateRoute() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val allPatients = db.patientDao().getAllPatients()
            val mappedPatients = allPatients.filter { it.latitude != 0.0 && it.longitude != 0.0 }

            if (mappedPatients.isNotEmpty()) {
                val remainingPatients = mappedPatients.filter { patient ->
                    visitedPatients.none { it.id == patient.id }
                }

                // If GPS invalid, use first patient location as start point
                val startLat = if (currentLat != 0.0) currentLat else remainingPatients.firstOrNull()?.latitude ?: 0.0
                val startLng = if (currentLng != 0.0) currentLng else remainingPatients.firstOrNull()?.longitude ?: 0.0

                if (remainingPatients.isNotEmpty()) {
                    val routeResult = RouteOptimizer.optimizeRoute(startLat, startLng, remainingPatients)
                    optimizedRoute = routeResult.toMutableList()
                    drawStaticRoute(optimizedRoute)
                } else {
                    drawStaticRoute(emptyList())
                }

                if (currentLat == 0.0 && remainingPatients.isNotEmpty()) {
                    map.controller.animateTo(GeoPoint(remainingPatients[0].latitude, remainingPatients[0].longitude))
                    Toast.makeText(this@MapRouteActivity, "Waiting for GPS...", Toast.LENGTH_SHORT).show()
                }
            } else {
                tvRouteInfo.text = "No patients with GPS data found."
                btnMarkVisited.isEnabled = false
            }
        }
    }

    private fun drawStaticRoute(patients: List<Patient>) {
        map.overlays.clear()

        // 1. Re-draw "You" (RED 🔴)
        myLocationMarker = null
        if (currentLat != 0.0 && currentLng != 0.0) {
            updateMyLocationMarker(GeoPoint(currentLat, currentLng))
        }

        // 2. Draw "Visited" Patients (GRAY ⚪)
        for (visited in visitedPatients) {
            val marker = Marker(map)
            marker.position = GeoPoint(visited.latitude, visited.longitude)
            marker.title = "Visited: ${visited.fullName}"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // ✅ FIX: Use standard Android system icon
            val icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_myplaces)?.mutate()

            // ⚪ Color: GRAY (Visited)
            icon?.setTint(Color.GRAY)
            marker.icon = icon

            map.overlays.add(marker)
        }

        if (patients.isEmpty()) {
            if (tvRouteInfo.text != "No patients with GPS data found.") {
                tvRouteInfo.text = "All visits completed!"
            }
            btnMarkVisited.isEnabled = false
            map.invalidate()
            return
        }

        btnMarkVisited.isEnabled = true
        val pathPoints = ArrayList<GeoPoint>()

        if (currentLat != 0.0 && currentLng != 0.0) {
            pathPoints.add(GeoPoint(currentLat, currentLng))
        }

        // 3. Draw Active Route Patients
        patients.forEachIndexed { index, patient ->
            val point = GeoPoint(patient.latitude, patient.longitude)
            pathPoints.add(point)

            val marker = Marker(map)
            marker.position = point
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // ✅ FIX: Use standard Android system icon
            val icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_myplaces)?.mutate()

            if (index == 0) {
                // 🟢 Color: GREEN (Next Patient)
                icon?.setTint(Color.parseColor("#4CAF50"))
                marker.title = "NEXT: ${patient.fullName}"
                marker.snippet = "Current Target"
            } else {
                // 🔵 Color: BLUE (Other Patients)
                icon?.setTint(Color.parseColor("#2196F3"))
                marker.title = "${index + 1}. ${patient.fullName}"
                marker.snippet = "Tap to prioritize"
            }

            marker.icon = icon

            marker.setOnMarkerClickListener { m, _ ->
                if (index != 0) {
                    prioritizePatient(patient)
                }
                m.showInfoWindow()
                true
            }
            map.overlays.add(marker)
        }

        // 4. Draw Blue Path Line
        if (pathPoints.size > 1) {
            val line = Polyline()
            line.setPoints(pathPoints)
            line.outlinePaint.color = Color.parseColor("#2196F3")
            line.outlinePaint.strokeWidth = 12f
            map.overlays.add(line)
        }

        tvRouteInfo.text = "Next: ${patients[0].fullName}"
        map.invalidate()
    }

    private fun refreshRoute() {
        Toast.makeText(this, "Recalculating...", Toast.LENGTH_SHORT).show()
        calculateRoute()
    }

    private fun markCurrentTargetAsVisited() {
        if (optimizedRoute.isNotEmpty()) {
            val visited = optimizedRoute[0]
            visitedPatients.add(visited)
            Toast.makeText(this, "Visited: ${visited.fullName}", Toast.LENGTH_SHORT).show()

            optimizedRoute.removeAt(0)

            // Snap current location to visited patient for smoother UI
            currentLat = visited.latitude
            currentLng = visited.longitude

            drawStaticRoute(optimizedRoute)
        } else {
            Toast.makeText(this, "All visits completed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun prioritizePatient(selectedPatient: Patient) {
        val index = optimizedRoute.indexOfFirst { it.id == selectedPatient.id }
        if (index > 0) {
            optimizedRoute.removeAt(index)
            optimizedRoute.add(0, selectedPatient)
            Toast.makeText(this, "Re-routing to: ${selectedPatient.fullName}", Toast.LENGTH_SHORT).show()
            drawStaticRoute(optimizedRoute)
        }
    }

    private fun startNavigation() {
        if (optimizedRoute.isNotEmpty()) {
            val nextStop = optimizedRoute[0]
            val uri = Uri.parse("google.navigation:q=${nextStop.latitude},${nextStop.longitude}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            try { startActivity(intent) } catch (e: Exception) {
                Toast.makeText(this, "Google Maps not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No destination selected", Toast.LENGTH_SHORT).show()
        }
    }
}