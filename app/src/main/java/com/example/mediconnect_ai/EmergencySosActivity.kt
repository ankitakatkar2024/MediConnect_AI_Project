package com.example.mediconnect_ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.firestore.FirebaseSosAlertHelper
import com.example.mediconnect_ai.utils.AgeUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

class EmergencySosActivity : BaseActivity() {

    private lateinit var btnSos: Button
    private lateinit var btnCancel: Button
    private lateinit var tvCountdown: TextView
    private lateinit var tvLocationAddress: TextView
    private lateinit var tvMedicalSummary: TextView

    private var countdownTimer: CountDownTimer? = null
    private var isSosActive = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Variables to hold the data for the alert
    private var patientId: Long = -1L
    private var medicalInfoString: String = "Fetching patient data..."
    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0

    companion object {
        const val EXTRA_PATIENT_ID = "patient_id"
    }

    // --- Permission Launchers ---
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fine || coarse) {
                if (::tvLocationAddress.isInitialized) fetchLocation()
            } else {
                if (::tvLocationAddress.isInitialized) tvLocationAddress.text = "Location permission required."
            }
        }

    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                sendSOSAlert()
            } else {
                Toast.makeText(this, "SMS permission denied. Cannot send alert.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_sos)

        // Initialize Views
        btnSos = findViewById(R.id.btnSos)
        btnCancel = findViewById(R.id.btnCancel)
        tvCountdown = findViewById(R.id.tvCountdown)
        tvLocationAddress = findViewById(R.id.tvLocationAddress)
        tvMedicalSummary = findViewById(R.id.tvMedicalSummary)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 1. Get Patient ID from intent
        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)

        // 2. Start gathering data immediately (Location + Medical DB)
        fetchLocation()
        fetchMedicalData()

        // 3. Setup Button Listeners
        btnSos.setOnClickListener {
            if (!isSosActive) startCountdown()
        }

        btnCancel.setOnClickListener {
            cancelCountdown()
        }
    }

    // --- DATA GATHERER: Fetch Patient Info from Room Database ---
    private fun fetchMedicalData() {
        if (patientId == -1L) {
            medicalInfoString = "Unknown Patient (No ID provided)"
            tvMedicalSummary.text = medicalInfoString
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val patient = db.patientDao().getPatientById(patientId)
            val pregnancy = db.pregnancyDao().getActivePregnancyForPatient(patientId)

            if (patient != null) {
                val age = AgeUtils.getFullAge(patient.dob)
                val conditions = if (patient.preExistingConditions.isNotEmpty())
                    patient.preExistingConditions else "None"
                val pregnancyStatus = if (pregnancy != null) {
                    "PREGNANT (LMP: ${java.text.SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(java.util.Date(pregnancy.lmpDate))})"
                } else {
                    "Not Pregnant"
                }

                medicalInfoString = """
                    🚨 EMERGENCY MEDICAL DATA 🚨
                    Name: ${patient.fullName}
                    Age: $age | Gender: ${patient.gender}
                    Status: $pregnancyStatus
                    History: $conditions
                    Phone: ${patient.contactNumber}
                """.trimIndent()

                tvMedicalSummary.text = medicalInfoString
            } else {
                medicalInfoString = "Error: Patient not found in Database."
                tvMedicalSummary.text = medicalInfoString
            }
        }
    }

    // --- DATA GATHERER: Fetch GPS Location ---
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                currentLat = loc.latitude
                currentLng = loc.longitude
                tvLocationAddress.text = getAddress(loc.latitude, loc.longitude)
            } else {
                tvLocationAddress.text = "Fetching accurate location..."
            }
        }
    }

    private fun getAddress(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (!list.isNullOrEmpty()) {
                list[0].getAddressLine(0)
            } else {
                "Lat: $lat, Lng: $lon"
            }
        } catch (e: Exception) {
            "Lat: $lat, Lng: $lon"
        }
    }

    // --- SOS LOGIC: Countdown ---
    private fun startCountdown() {
        isSosActive = true
        btnSos.visibility = View.INVISIBLE
        tvCountdown.visibility = View.VISIBLE
        btnCancel.visibility = View.VISIBLE

        countdownTimer = object : CountDownTimer(5_000, 1_000) {
            override fun onTick(ms: Long) {
                tvCountdown.text = ((ms / 1000) + 1).toString()
            }

            override fun onFinish() {
                sendSOSAlert()
                resetUI()
            }
        }.start()
    }

    private fun cancelCountdown() {
        countdownTimer?.cancel()
        resetUI()
        Toast.makeText(this, "SOS Canceled", Toast.LENGTH_SHORT).show()
    }

    private fun resetUI() {
        isSosActive = false
        btnSos.visibility = View.VISIBLE
        tvCountdown.visibility = View.GONE
        btnCancel.visibility = View.GONE
    }

    // --- TRANSMISSION: Send SMS + log to Firestore ---
    private fun sendSOSAlert() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            return
        }

        // 2. GET THE SAVED NUMBER  (use same prefs as EmergencyContactsActivity)
        val prefs = getSharedPreferences("EmergencyPrefs", Context.MODE_PRIVATE)
        val savedNumber = prefs.getString("sos_number", "") ?: ""

        if (savedNumber.isEmpty() || savedNumber == "108") {
            Toast.makeText(
                this,
                "⚠️ Please Configure a valid Phone Number in Settings! Cannot send to 108 automatically.",
                Toast.LENGTH_LONG
            ).show()
            return
        }


        Toast.makeText(this, "Sending Alert to: $savedNumber", Toast.LENGTH_SHORT).show()

        val googleMapsLink = "http://maps.google.com/?q=$currentLat,$currentLng"
        val addressFull = tvLocationAddress.text.toString()
        val addressShort =
            if (addressFull.length > 50) addressFull.take(50) + "..." else addressFull

        // 4. Prepare SMS Message
        val finalAlertMessage =
            "$medicalInfoString\n\n📍 LOC: $googleMapsLink\nAddr: $addressShort"

        // 5. LOG TO FIRESTORE (sos_alerts)
        val alert = SosAlert(
            patientId = patientId,
            medicalSummary = medicalInfoString,
            latitude = currentLat,
            longitude = currentLng,
            address = addressFull,
            sentTo = savedNumber,
            timestamp = System.currentTimeMillis()
        )

        FirebaseSosAlertHelper.saveAlert(alert) { success, error ->
            // optional: you can log or toast on failure
            if (!success) {
                // Toast.makeText(this, "Cloud log failed: $error", Toast.LENGTH_SHORT).show()
            }
        }

        // 6. Actually send the SMS
        sendSmsInternal(savedNumber, finalAlertMessage)
    }

    private fun sendSmsInternal(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
                } else {
                    SmsManager.getDefault()
                }

            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)

            Toast.makeText(this, "🚨 SMS Alert Sent Successfully!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStop() {
        super.onStop()
        countdownTimer?.cancel()
    }
}
