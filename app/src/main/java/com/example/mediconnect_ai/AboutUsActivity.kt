package com.example.mediconnect_ai

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.example.mediconnect_ai.databinding.ActivityAboutUsBinding

class AboutUsActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup the toolbar and enable the back arrow for navigation
        setSupportActionBar(binding.toolbarAbout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Dynamically get the app version name and display it
        try {
            val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).versionName
            }
            binding.tvAppVersion.text = "Version $versionName"
        } catch (e: Exception) {
            // In case of an error, fall back to a default version
            binding.tvAppVersion.text = "Version 1.0.0"
        }
    }

    // This function handles the click of the back arrow in the toolbar,
    // allowing the user to navigate back to the previous screen.
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

