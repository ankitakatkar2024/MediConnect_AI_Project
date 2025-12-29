package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediconnect_ai.adapters.ReferredPatientAdapter
import com.example.mediconnect_ai.database.AppDatabase
import com.example.mediconnect_ai.database.Patient
import com.example.mediconnect_ai.databinding.ActivityDoctorDashboardBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorDashboardActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityDoctorDashboardBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: ReferredPatientAdapter

    companion object {
        private const val PREFS_NAME = "MediConnectPrefs"
        private const val KEY_USER_NAME = "USER_NAME"
        private const val KEY_USER_AREA = "USER_AREA"
        private const val KEY_USER_ROLE = "USER_ROLE"
        private const val KEY_USER_ROLE_DISPLAY = "USER_ROLE_DISPLAY"
        private const val KEY_USER_CONTACT = "USER_CONTACT"
        private const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        private const val KEY_USER_MPIN = "USER_MPIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        val toolbar: Toolbar = binding.toolbarDoctorDashboard
        setSupportActionBar(toolbar)

        // Drawer setup
        drawerLayout = binding.drawerLayoutDoctor
        val navView: NavigationView = binding.navViewDoctor
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // RecyclerView setup
        adapter = ReferredPatientAdapter(
            onProfileClick = { patient ->
                val i = Intent(this, PatientDashboardActivity::class.java)
                i.putExtra(PatientDashboardActivity.EXTRA_PATIENT_ID, patient.id)
                startActivity(i)
            },
            onResolveClick = { patient ->
                resolveReferral(patient)
            }
        )

        binding.referredPatientsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.referredPatientsRecyclerView.adapter = adapter

        populateProfileFromPrefs()
        observeReferredPatients()
    }

    // Loads doctor name/area/role/contact from SharedPreferences
    private fun populateProfileFromPrefs() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_USER_NAME, "Doctor") ?: "Doctor"
        val area = prefs.getString(KEY_USER_AREA, null)
        val roleDisplay = prefs.getString(KEY_USER_ROLE_DISPLAY, prefs.getString(KEY_USER_ROLE, null))
        val contact = prefs.getString(KEY_USER_CONTACT, "") ?: ""

        // Welcome Section
        binding.tvWelcomeName.text = name

        // Profile Card
        binding.tvProfileName.text = name
        binding.tvProfileContact.text = if (contact.isNotEmpty()) contact else "—"

        val subtitle = listOfNotNull(
            roleDisplay?.takeIf { it.isNotBlank() },
            area?.takeIf { it.isNotBlank() }
        ).joinToString(" • ")

        binding.tvProfileRoleArea.text = if (subtitle.isNotBlank()) subtitle else "Doctor"

        // Navigation Drawer Header Update
        val nav = binding.navViewDoctor
        if (nav.headerCount > 0) {
            val header = nav.getHeaderView(0)
            val title = header.findViewById<android.widget.TextView>(R.id.nav_header_title)
            val sub = header.findViewById<android.widget.TextView>(R.id.nav_header_subtitle)

            title?.text = name
            sub?.text = if (subtitle.isNotBlank()) subtitle else "Doctor Dashboard"
        }
    }

    // Observe referred (unresolved) patients via LiveData
    private fun observeReferredPatients() {
        val dao = AppDatabase.getInstance(applicationContext).patientDao()
        dao.getReferredPatients().observe(this) { list ->
            adapter.submitList(list)
            updateAnalytics(list)
        }
    }

    // Update pending/resolved counters
    private fun updateAnalytics(list: List<Patient>) {
        val pending = list.count { !it.isResolved }
        val resolved = list.count { it.isResolved }

        binding.tvStatPendingCount.text = pending.toString()
        binding.tvStatResolvedCount.text = resolved.toString()
    }

    // Clean, coroutine-based resolve function using DAO
    private fun resolveReferral(patient: Patient) {
        Toast.makeText(this, "Resolving case for ${patient.fullName}...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(applicationContext)
                        .patientDao()
                        .markResolvedById(patient.id)
                }

                Toast.makeText(this@DoctorDashboardActivity, "Marked resolved.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@DoctorDashboardActivity,
                    "Unable to update DB automatically. Please update manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_doctor_dashboard -> {}
            R.id.nav_doctor_profile ->
                Toast.makeText(this, "Doctor Profile screen coming soon.", Toast.LENGTH_SHORT).show()
            R.id.nav_doctor_logout -> confirmLogout()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
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
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_AREA)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_ROLE_DISPLAY)
            .remove(KEY_USER_CONTACT)
            .remove(KEY_USER_MPIN)
            .apply()

        val i = Intent(this, WelcomeActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        finish()
    }
}
