package com.example.mediconnect_ai

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {

    private const val PREFS_NAME = "MediConnectPrefs"
    private const val PREF_LANG_KEY = "app_language_code"
    private const val DEFAULT_LANG = "en" // Default language if none is set
    private const val TAG = "LocaleManager" // Tag for logging

    // ✅ FIXED: The supported languages are now taken directly from your central LanguageList.
    // This is now the single source of truth.
    val supportedLanguages by lazy { LanguageList.languages.map { it.code } }

    // --- SharedPreferences Management ---
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getPersistedLanguage(context: Context): String {
        return getPrefs(context).getString(PREF_LANG_KEY, DEFAULT_LANG) ?: DEFAULT_LANG
    }

    private fun persistLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(PREF_LANG_KEY, languageCode).apply()
    }

    // --- Locale Application Logic ---

    fun applyPersistedLocale(context: Context) {
        val languageCode = getPersistedLanguage(context)
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        Locale.setDefault(Locale(languageCode))
    }

    fun setNewLocale(context: Context, languageCode: String) {
        if (languageCode !in supportedLanguages) {
            Log.e(TAG, "Attempted to set an unsupported language: $languageCode")
            return
        }
        persistLanguage(context, languageCode)
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        // No need to set Locale.setDefault here, as the configuration change
        // triggered by setApplicationLocales will handle it.
    }

    fun getCurrentAppLocale(): Locale? {
        val localeList = AppCompatDelegate.getApplicationLocales()
        return if (localeList.isEmpty) null else localeList[0]
    }

    /**
     * Wraps the base context of an Activity with a new context that has the
     * correct locale configuration. This is crucial for ensuring that Activities
     * load the correct resources (strings, layouts) on creation.
     */
    fun wrapContext(context: Context): Context {
        val savedLanguage = getPersistedLanguage(context)
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}

