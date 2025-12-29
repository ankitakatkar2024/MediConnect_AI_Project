package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.databinding.ActivityProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Calendar

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val PREFS_NAME = "MediConnectPrefs"
        const val KEY_USER_ID = "USER_ID"
        const val KEY_USER_NAME = "USER_NAME"
        const val KEY_USER_ROLE = "USER_ROLE"
        const val KEY_ASSIGNED_AREA = "ASSIGNED_AREA"
        const val KEY_USER_PHOTO_URI = "USER_PHOTO_URI"
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Allow permanent access to selected image
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY_USER_PHOTO_URI, it.toString()).apply()

                // Update immediately
                binding.ivProfilePicture.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbarProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Image selection
        binding.ivProfilePicture.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Load locally first, then refresh from Firestore
        loadProfileData()
        refreshFromFirestore()
    }

    override fun onResume() {
        super.onResume()
        // Reload every time user returns
        loadProfileData()
    }

    /**
     * Load user details from SharedPreferences
     */
    private fun loadProfileData() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, "N/A")
        val userName = prefs.getString(KEY_USER_NAME, "Guest User")
        val userRole = prefs.getString(KEY_USER_ROLE, "User")
        val assignedArea = prefs.getString(KEY_ASSIGNED_AREA, "Unassigned")
        val photoUriString = prefs.getString(KEY_USER_PHOTO_URI, null)

        // Update UI with saved data
        binding.tvUserName.text = userName
        binding.tvUserId.text = "${formatRoleName(userRole)} ID: $userId"
        binding.tvAssignedArea.text = "Assigned Area: $assignedArea"

        if (!photoUriString.isNullOrEmpty()) {
            binding.ivProfilePicture.setImageURI(Uri.parse(photoUriString))
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
        }

        // Load performance info
        loadPerformanceData(userId)
    }

    /**
     * Refresh latest details from Firestore
     */
    private fun refreshFromFirestore() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString(KEY_USER_ID, null) ?: return

        // Try to fetch using the same customId instead of Firebase UID
        db.collection("users")
            .whereEqualTo("customId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val name = document.getString("fullName") ?: "Guest User"
                    val role = document.getString("role") ?: "User"
                    val area = document.getString("district") ?: "Unassigned"

                    // Save updated data locally
                    prefs.edit().apply {
                        putString(KEY_USER_NAME, name)
                        putString(KEY_USER_ROLE, role)
                        putString(KEY_ASSIGNED_AREA, area)
                    }.apply()

                    // Update UI immediately
                    binding.tvUserName.text = name
                    binding.tvUserId.text = "${formatRoleName(role)} ID: $userId"
                    binding.tvAssignedArea.text = "Assigned Area: $area"
                }
            }
    }

    /**
     * Fetch performance stats for user
     */
    private fun loadPerformanceData(userId: String?) {
        if (userId.isNullOrEmpty() || userId == "N/A") return

        lifecycleScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            val patientDao = db.patientDao()
            val pregnancyDao = db.pregnancyDao()

            val totalPatients = patientDao.getTotalPatientCountForUser(userId)
            val activePregnancies = pregnancyDao.getActivePregnanciesForUser(userId).size
            val twoYearsAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }.timeInMillis
            val infantsTracked = patientDao.getInfantsTrackedForUser(userId, twoYearsAgo).size

            binding.tvTotalPatients.text = "Total Patients Registered: $totalPatients"
            binding.tvActivePregnancies.text = "Active Pregnancies: $activePregnancies"
            binding.tvInfantsTracked.text = "Infants Tracked: $infantsTracked"
        }
    }

    /**
     * Helper to display readable role names
     */
    private fun formatRoleName(role: String?): String {
        return when (role?.lowercase()) {
            "doctor" -> "Doctor"
            "asha_anm_nurse" -> "ASHA / ANM Nurse"
            "patient_family" -> "Patient / Family Member"
            else -> "User"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
