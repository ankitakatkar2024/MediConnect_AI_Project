package com.example.mediconnect_ai

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * A base activity that all other activities in the app should extend.
 * It handles applying the correct locale configuration to the activity's context,
 * ensuring that all UI elements and resources are displayed in the user's selected language.
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Intercept the context creation and wrap it with our LocaleManager.
        // This ensures the correct language is applied before the activity creates its views.
        // FIXED: Corrected the typo from "attachBase-Context" to "attachBaseContext"
        super.attachBaseContext(LocaleManager.wrapContext(newBase))
    }
}

