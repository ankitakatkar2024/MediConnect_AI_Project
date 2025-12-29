package com.example.mediconnect_ai

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    /**
     * Sets the app's locale to the provided language code and returns
     * a wrapped context with the correct configuration.
     *
     * @param context Original context from BaseActivity or Application
     * @param languageCode Language code like "en", "hi", "mr", "pa", etc.
     * @return Context with updated locale applied
     */
    fun setLocale(context: Context, languageCode: String): Context {
        // Normalize languageCode (avoid nulls, fallback to "en")
        val lang = languageCode.ifEmpty { "en" }
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        // ✅ Use setLocale() and also set layout direction (important for RTL languages like Urdu)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // For Android N (7.0) and above - use createConfigurationContext
            context.createConfigurationContext(config)
        } else {
            // For older Android versions, update resources directly
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
