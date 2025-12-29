package com.example.mediconnect_ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.example.mediconnect_ai.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val languages = arrayOf("English", "हिंदी (Hindi)")
    private val languageCodes = arrayOf("en", "hi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.languageSettingLayout.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // This line will now work correctly
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // Save the notification preference
            // For example: savePreference("NOTIFICATIONS_ENABLED", isChecked)
        }
    }

    private fun showLanguageSelectionDialog() {
        val currentLanguageCode = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag() ?: Locale.getDefault().language
        val checkedItem = languageCodes.indexOf(currentLanguageCode).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_select_language_title))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val selectedLanguageCode = languageCodes[which]
                setLocale(selectedLanguageCode)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun setLocale(languageCode: String) {
        val prefs = getSharedPreferences("MediConnectPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("APP_LANGUAGE", languageCode).apply()

        val appLocale = AppCompatDelegate.getApplicationLocales()
        if (appLocale.isEmpty || appLocale[0]?.toLanguageTag() != languageCode) {
            val newLocale = androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(newLocale)
        }

        val intent = Intent(this, WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}