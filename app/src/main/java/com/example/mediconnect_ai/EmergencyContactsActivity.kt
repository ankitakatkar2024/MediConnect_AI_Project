package com.example.mediconnect_ai

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EmergencyContactsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmergencyContactsAdapter
    private lateinit var btnViewLogs: Button

    // Views for Saving Number
    private lateinit var etSosNumber: EditText
    private lateinit var btnSaveNumber: Button

    // SharedPreferences
    private val PREFS_NAME = "EmergencyPrefs"
    private val KEY_SOS_NUMBER = "sos_number"

    // sample contacts (replace with DB / prefs when ready)
    private val contacts = listOf(
        EmergencyContact("Ankita katkar", "Personal", "+918591036401", R.drawable.ic_person_log),
        EmergencyContact("Ambulance", "Emergency", "108", R.drawable.ic_person_log),
        EmergencyContact("Police", "Emergency", "100", R.drawable.ic_person_log),
        EmergencyContact("Fire Brigade", "Emergency", "101", R.drawable.ic_person_log),
        EmergencyContact("Dr. Suman", "Doctor", "+918850119669", R.drawable.ic_person_log),
        EmergencyContact("Nurse Meera", "Nurse", "+918104772032", R.drawable.ic_person_log),
        EmergencyContact("Nurse Aditi", "Nurse", "+919769411778", R.drawable.ic_person_log)
    )

    // store phone number when launching image picker so we know which contact to update
    private var currentPhotoTargetPhone: String? = null

    // If a call was requested but permission was not granted yet, keep pending number
    private var pendingCallPhone: String? = null

    // Permission launcher for CALL_PHONE
    private val requestCallPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingCallPhone?.let { phone ->
                pendingCallPhone = null
                performCall(phone)
            }
        } else {
            Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Image picker launcher
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentPhotoTargetPhone?.let { phone ->
                adapter.setPhotoUriFor(phone, it.toString())
            }
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contacts)

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerEmergencyContacts)
        btnViewLogs = findViewById(R.id.btnViewLogs)
        etSosNumber = findViewById(R.id.etSosNumber)
        btnSaveNumber = findViewById(R.id.btnSaveNumber)

        // SharedPreferences Setup
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val savedNumber = sharedPref.getString(KEY_SOS_NUMBER, "")
        etSosNumber.setText(savedNumber)

        btnSaveNumber.setOnClickListener {
            val number = etSosNumber.text.toString().trim()
            if (number.isNotEmpty()) {
                sharedPref.edit()
                    .putString(KEY_SOS_NUMBER, number)
                    .apply()
                Toast.makeText(this, "✅ Doctor's Number Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = EmergencyContactsAdapter(
            contactsList = contacts,
            lifecycleScope = lifecycleScope,
            onCallClicked = { phone ->
                confirmAndCall(phone)
            },
            onProfilePhotoClicked = { phone ->
                currentPhotoTargetPhone = phone
                pickImage.launch("image/*")
            }
        )

        recyclerView.adapter = adapter

        btnViewLogs.setOnClickListener {
            startActivity(Intent(this, SosLogsActivity::class.java))
        }
    }

    private fun confirmAndCall(phoneNumber: String) {
        AlertDialog.Builder(this)
            .setTitle("Call")
            .setMessage("Do you want to call $phoneNumber ?")
            .setPositiveButton("Yes") { _, _ -> performCallWithPermissionFlow(phoneNumber) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performCallWithPermissionFlow(phoneNumber: String) {
        if (isRunningOnEmulator()) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialIntent)
                Toast.makeText(
                    this,
                    "Opened dialer (emulator). Use a real device to place actual calls.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            performCall(phoneNumber)
        } else {
            pendingCallPhone = phoneNumber
            requestCallPermission.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun performCall(phoneNumber: String) {
        if (isRunningOnEmulator()) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                startActivity(dialIntent)
                Toast.makeText(this, "Opened dialer (emulator).", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Unable to start call", Toast.LENGTH_SHORT).show()
            }
        } else {
            pendingCallPhone = phoneNumber
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                1234
            )
        }
    }

    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.lowercase().contains("vbox")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BOARD.lowercase().contains("nox"))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1234) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pendingCallPhone?.let {
                    performCall(it)
                    pendingCallPhone = null
                }
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
