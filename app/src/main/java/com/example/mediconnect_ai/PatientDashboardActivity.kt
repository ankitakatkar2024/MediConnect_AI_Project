package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.firestore.FirebasePatientHelper
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.util.Calendar

class PatientDashboardActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var patientId: Long = -1L
    private lateinit var drawerLayout: DrawerLayout

    companion object {
        const val EXTRA_PATIENT_ID = "EXTRA_PATIENT_ID"
        private const val PREFS_NAME = "MediConnectPrefs"
        private const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        private const val KEY_USER_ROLE = "USER_ROLE"
        private const val KEY_USER_MPIN = "USER_MPIN"
        private const val KEY_MPIN_VERIFIED_SESSION = "MPIN_VERIFIED_SESSION"
        private const val KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_dashboard)

        patientId = intent.getLongExtra(EXTRA_PATIENT_ID, -1L)

        // --- Drawer & Toolbar setup ---
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (patientId == -1L) {
            Toast.makeText(this, "Error: Patient ID not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPatientData()
        setupClickListeners()
    }

    // Drawer item clicks
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_edit_patient -> {
                Toast.makeText(
                    this,
                    "Please contact your ASHA worker to edit details.",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.nav_emergency_contacts -> {
                startActivity(Intent(this, EmergencyContactsActivity::class.java))
            }
            R.id.nav_logout -> {
                confirmLogout()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * NEW LOGIC:
     * 1. Try to load patient from local Room DB.
     * 2. If not found, fetch from Firestore using the same numeric ID.
     * 3. If found from Firestore, show it & cache into Room.
     */
    private fun loadPatientData() {
        lifecycleScope.launch {
            val dbRoom = AppDatabase.getInstance(applicationContext)
            val patientDao = dbRoom.patientDao()

            // 1️⃣ First try local Room (works on ASHA phone and if cached on patient phone)
            val localPatient = patientDao.getPatientById(patientId)

            if (localPatient != null) {
                bindPatientToUi(localPatient)
            } else {
                // 2️⃣ If not in Room, get from Firestore (cloud)
                FirebasePatientHelper.fetchPatientById(patientId) { remotePatient, error ->
                    if (remotePatient != null) {
                        // Show data in UI
                        bindPatientToUi(remotePatient)

                        // 3️⃣ Cache into Room for offline use
                        lifecycleScope.launch {
                            try {
                                patientDao.insert(remotePatient)
                            } catch (_: Exception) {
                                // ignore cache errors
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@PatientDashboardActivity,
                            "No patient found for this ID.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    /** Common function to display patient data on screen */
    private fun bindPatientToUi(patient: Patient) {
        val tvName = findViewById<TextView>(R.id.tv_patient_name_header)
        val tvHeaderDetails = findViewById<TextView>(R.id.tv_patient_details_header)
        val tvContact = findViewById<TextView>(R.id.tv_patient_contact)
        val tvAddress = findViewById<TextView>(R.id.tv_patient_address)

        tvName.text = patient.fullName

        val dobCalendar = Calendar.getInstance().apply { timeInMillis = patient.dob }
        val today = Calendar.getInstance()
        var ageInYears = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
            ageInYears--
        }

        tvHeaderDetails.text = "${patient.gender}, $ageInYears years old"
        tvContact.text = "Contact: ${patient.contactNumber.ifEmpty { "N/A" }}"
        tvAddress.text = "Address: ${patient.address.ifEmpty { "N/A" }}"

        supportActionBar?.title = "My Health Dashboard"

        val immunizationCard = findViewById<MaterialCardView>(R.id.card_view_immunization)
        val pregnancyCard = findViewById<MaterialCardView>(R.id.card_view_pregnancy)

        // show/hide cards based on age & gender
        if (ageInYears < 18) {
            immunizationCard.visibility = View.VISIBLE
        } else {
            immunizationCard.visibility = View.GONE
        }

        if (patient.gender.equals("Female", ignoreCase = true) && ageInYears in 15..49) {
            pregnancyCard.visibility = View.VISIBLE
        } else {
            pregnancyCard.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        findViewById<MaterialCardView>(R.id.card_view_immunization).setOnClickListener {
            val intent = Intent(this, ImmunizationScheduleActivity::class.java)
            intent.putExtra(ImmunizationScheduleActivity.EXTRA_PATIENT_ID, patientId)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.card_view_pregnancy).setOnClickListener {
            Toast.makeText(this, "Pregnancy details screen to be built.", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialCardView>(R.id.card_view_tasks).setOnClickListener {
            val intent = Intent(this, PatientTasksActivity::class.java)
            intent.putExtra(PatientTasksActivity.EXTRA_PATIENT_ID, patientId)
            startActivity(intent)
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> performLogout() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_MPIN_VERIFIED_SESSION, false)
            .putBoolean(KEY_IS_FIRST_LAUNCH, true)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_MPIN)
            .apply()

        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
