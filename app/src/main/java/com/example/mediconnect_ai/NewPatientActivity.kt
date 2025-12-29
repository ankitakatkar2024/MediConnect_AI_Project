package com.example.mediconnect_ai

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.database.VaccineStatus
import com.example.mediconnect_ai.databinding.ActivityNewPatientBinding
import com.example.mediconnect_ai.firestore.FirebasePatientHelper
import com.example.mediconnect_ai.utils.AgeUtils
import com.example.mediconnect_ai.utils.VaccineScheduleGenerator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NewPatientActivity : BaseActivity() {

    private lateinit var binding: ActivityNewPatientBinding
    private var selectedDob: Calendar? = null
    private var currentPatientId: Long? = null

    // Location variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    companion object {
        const val EXTRA_PATIENT_ID = "EXTRA_PATIENT_ID"
        private const val TAG = "NewPatientActivity"
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            val fine = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarse = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fine || coarse) {
                fetchLocation()
            } else {
                binding.tvGpsStatus.text = "Permission denied"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        currentPatientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L).takeIf { it != -1L }
        binding.btnSavePatient.text = getString(R.string.save_patient)
        supportActionBar?.title =
            if (currentPatientId != null) "Edit Patient" else getString(R.string.register_new_patient)

        binding.btnSelectDob.setOnClickListener { showDatePicker() }
        binding.btnSavePatient.setOnClickListener { savePatient() }
        binding.btnCaptureGps.setOnClickListener { checkLocationPermissions() }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        binding.tvGpsStatus.text = "Fetching GPS..."

        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                currentLatitude = loc.latitude
                currentLongitude = loc.longitude
                binding.tvGpsStatus.text =
                    "Captured: ${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)}"
                Toast.makeText(this, "Location Captured!", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvGpsStatus.text = "Unable to get location"
            }
        }.addOnFailureListener {
            binding.tvGpsStatus.text = "Error getting location"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDob = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                updateDobTextView()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    private fun updateDobTextView() {
        selectedDob?.let {
            val fullAge = AgeUtils.getFullAge(it.timeInMillis)
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            binding.tvSelectedDob.text = "${dateFormat.format(it.time)} (Age: $fullAge)"
        }
    }

    private fun savePatient() {
        val name = binding.etPatientName.text.toString().trim()
        val contact = binding.etContactNumber.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val conditions = binding.etConditions.text.toString().trim()
        val aadhaar = binding.etAadhaarNumber.text.toString().trim()

        // 🔹 NEW: household fields
        val familyId = binding.etFamilyId.text.toString().trim()
        val householdSizeText = binding.etHouseholdSize.text.toString().trim()
        val householdSize = householdSizeText.toIntOrNull() ?: 0
        val isHeadOfFamily = binding.cbHeadOfFamily.isChecked

        if (name.isEmpty() || selectedDob == null) {
            toast("Please fill Name and Date of Birth")
            return
        }

        val genderId = binding.rgGender.checkedRadioButtonId
        if (genderId == -1) {
            toast("Please select a gender")
            return
        }

        val gender = when (genderId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> "Other"
        }

        val prefs = getSharedPreferences(ProfileActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val currentAshaId = prefs.getString(ProfileActivity.KEY_USER_ID, null)
        if (currentAshaId == null) {
            toast("Error: User not logged in.")
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val patientDao = db.patientDao()

            val patient = Patient(
                id = currentPatientId ?: 0L,
                fullName = name,
                dob = selectedDob!!.timeInMillis,
                registrationTimestamp = System.currentTimeMillis(),
                isReferred = false,
                gender = gender,
                contactNumber = contact,
                address = address,
                preExistingConditions = conditions,
                aadhaarNumber = aadhaar,
                assignedAshaId = currentAshaId,
                latitude = currentLatitude,
                longitude = currentLongitude,
                // 🔹 NEW FIELDS
                familyId = if (familyId.isNotEmpty()) familyId else null,
                householdSize = householdSize,
                isHeadOfFamily = isHeadOfFamily
            )

            if (currentPatientId == null) {
                // NEW PATIENT
                val newPatientId = patientDao.insert(patient)
                toast("Patient saved locally with ID: $newPatientId")

                FirebasePatientHelper.savePatient(patient.copy(id = newPatientId)) { success, error ->
                    if (success) {
                        Log.d(TAG, "Cloud sync OK for patientId=$newPatientId")
                        Toast.makeText(
                            this@NewPatientActivity,
                            "✅ Synced to cloud (ID: $newPatientId)",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(TAG, "Cloud sync FAILED for patientId=$newPatientId: $error")
                        Toast.makeText(
                            this@NewPatientActivity,
                            "⚠️ Cloud sync failed, saved only on this phone.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                handleNewPatientSchedule(newPatientId)
            } else {
                // UPDATE EXISTING PATIENT
                patientDao.update(patient)
                toast("Patient updated locally!")

                FirebasePatientHelper.savePatient(patient) { success, error ->
                    if (success) {
                        Log.d(TAG, "Cloud update OK for patientId=${patient.id}")
                    } else {
                        Log.e(TAG, "Cloud update FAILED for patientId=${patient.id}: $error")
                        Toast.makeText(
                            this@NewPatientActivity,
                            "⚠️ Update failed in cloud, will only show on this phone.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                finish()
            }
        }
    }

    private suspend fun handleNewPatientSchedule(patientId: Long) {
        val ageInYears = AgeUtils.getYears(selectedDob!!.timeInMillis)
        if (ageInYears < 5) {
            val schedule =
                VaccineScheduleGenerator.generateSchedule(Date(selectedDob!!.timeInMillis))
            val vaccineStatuses = schedule.map { vaccine ->
                VaccineStatus(
                    patientId = patientId,
                    vaccineName = vaccine.name,
                    dueDate = vaccine.dueDate.time,
                    status = "Scheduled"
                )
            }
            val db = AppDatabase.getInstance(applicationContext)
            db.vaccineStatusDao().insertAll(vaccineStatuses)

            startActivity(
                Intent(this, ImmunizationScheduleActivity::class.java).apply {
                    putExtra(ImmunizationScheduleActivity.EXTRA_DOB, selectedDob!!.timeInMillis)
                    putExtra(ImmunizationScheduleActivity.EXTRA_PATIENT_ID, patientId)
                }
            )
            finish()
        } else {
            showAddAnotherPatientDialog()
        }
    }

    private fun showAddAnotherPatientDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add Family Member")
            .setMessage("Add another patient from this family?")
            .setPositiveButton("Yes") { _, _ -> clearForm() }
            .setNegativeButton("No") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun clearForm() {
        with(binding) {
            etPatientName.text?.clear()
            etContactNumber.text?.clear()
            etAadhaarNumber.text?.clear()
            etConditions.text?.clear()
            rgGender.clearCheck()
            tvSelectedDob.text = "No date selected"
            tvGpsStatus.text = "Location not set"

            // KEEP these so ASHA doesn’t re-enter for each member:
            // etAddress       -> keep as is
            // etFamilyId      -> keep as is
            // etHouseholdSize -> keep as is

            cbHeadOfFamily.isChecked = false   // next member is usually NOT head
        }
        selectedDob = null
        currentLatitude = 0.0
        currentLongitude = 0.0
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
