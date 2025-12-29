package com.example.mediconnect_ai

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

class WelcomeActivity : BaseActivity() {

    // ✅ Use same PREFS_NAME and KEY_IS_FIRST_LAUNCH as MainActivity
    companion object {
        const val PREFS_NAME = "MediConnectPrefs"
        const val KEY_IS_FIRST_LAUNCH = "IS_FIRST_LAUNCH"
    }

    private val languageSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val languageCode = result.data?.getStringExtra("SELECTED_LANG_CODE")
            if (languageCode != null) {
                LocaleManager.setNewLocale(this, languageCode)
                recreate() // 🔄 Refresh activity so texts update immediately
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)

        if (!isFirstLaunch) {
            // ✅ Skip WelcomeActivity if already seen → go to SplashActivity
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }

        // ✅ Show Welcome screen
        setContentView(R.layout.activity_welcome)

        val languageSelectorContainer: LinearLayout = findViewById(R.id.language_selector_container)
        val agreeButton: MaterialButton = findViewById(R.id.agree_button)
        val subtitleTextView: TextView = findViewById(R.id.welcome_subtitle)

        // ✅ Make Privacy Policy text clickable
        val htmlText = getString(R.string.privacy_policy_and_terms)
        subtitleTextView.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
        subtitleTextView.movementMethod = LinkMovementMethod.getInstance()

        // ✅ Open Language Selection screen
        languageSelectorContainer.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languageSelectionLauncher.launch(intent)
        }

        // ✅ On Agree → mark first launch as complete & go to Splash
        agreeButton.setOnClickListener {
            prefs.edit()
                .putBoolean(KEY_IS_FIRST_LAUNCH, false)
                .apply()

            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }
}
