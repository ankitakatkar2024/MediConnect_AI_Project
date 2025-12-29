package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.example.mediconnect_ai.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Organize setup into clean functions
        setupToolbarAndDrawer()
        setupBottomNavigation() // Added call to the new function
        setupCardListeners()
        setupNavigation()
        setupOnBackPressed()
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // REMOVED: The old toolbar menu click listener is no longer needed
        // because the icons are now in the bottom navigation bar.
    }

    // NEW FUNCTION: Handles clicks on the Bottom Navigation Bar
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_sos -> {
                    startActivity(Intent(this, EmergencySosActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupCardListeners() {
        binding.cardDailyTasks.setOnClickListener {
            startActivity(Intent(this, DailyTasksActivity::class.java))
        }

        // NEW: Smart Route Map Listener
        binding.cardRouteMap.setOnClickListener {
            startActivity(Intent(this, MapRouteActivity::class.java))
        }

        // NEW: Community Alerts Listener
        binding.cardCommunityAlerts.setOnClickListener {
            startActivity(Intent(this, SendAlertActivity::class.java))
        }

        binding.cardNewPatient.setOnClickListener {
            startActivity(Intent(this, NewPatientActivity::class.java))
        }
        // Inside setupCardListeners() function
        binding.cardOutbreakMap.setOnClickListener {
            // Navigate to the AI Map Screen we created earlier
            startActivity(Intent(this, OutbreakMapActivity::class.java))
        }
        binding.cardViewRecords.setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }
        binding.cardSymptomChecker.setOnClickListener {
            startActivity(Intent(this, SymptomCheckerActivity::class.java))
        }
        binding.cardQuickStats.setOnClickListener {
            startActivity(Intent(this, QuickStatsActivity::class.java))
        }
        binding.cardImmunizationSchedules.setOnClickListener {
            startActivity(Intent(this, ImmunizationHubActivity::class.java))
        }
    }

    private fun setupNavigation() {
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> { /* Already here */ }
            R.id.nav_resources -> startActivity(Intent(this, ResourcesActivity::class.java))
            R.id.nav_emergency_contacts -> startActivity(Intent(this, EmergencyContactsActivity::class.java))
            R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_about_us -> startActivity(Intent(this, AboutUsActivity::class.java))
            R.id.nav_logout -> confirmLogout()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_dialog_title))
            .setMessage(getString(R.string.logout_dialog_message))
            .setPositiveButton(getString(R.string.logout_dialog_yes)) { _, _ -> performLogout() }
            .setNegativeButton(getString(R.string.logout_dialog_no), null)
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

        startActivity(Intent(this, WelcomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    companion object {
        private const val PREFS_NAME = "MediConnectPrefs"
        private const val KEY_IS_LOGGED_IN = "IS_LOGGED_IN"
        private const val KEY_USER_ROLE = "USER_ROLE"
        private const val KEY_USER_MPIN = "USER_MPIN"
        private const val KEY_MPIN_VERIFIED_SESSION = "MPIN_VERIFIED_SESSION"
        private const val KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"
    }
}