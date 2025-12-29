package com.example.mediconnect_ai

/**
 * A centralized list of languages supported by the application.
 * This list now focuses on major languages spoken in India.
 */
object LanguageList {

    val languages = listOf(
        Language("en", "English", "(English)"), // Official language
        Language("hi", "हिन्दी", "(Hindi)"),
        Language("mr", "मराठी", "(Marathi)"),
        Language("bn", "বাংলা", "(Bengali)"),
        Language("te", "తెలుగు", "(Telugu)"),
        Language("ta", "தமிழ்", "(Tamil)"),
        Language("gu", "ગુજરાતી", "(Gujarati)"),
        Language("kn", "ಕನ್ನಡ", "(Kannada)"),
        Language("ml", "മലയാളം", "(Malayalam)"),
        Language("pa", "ਪੰਜਾਬੀ", "(Punjabi)"),
        Language("ur", "اردو", "(Urdu)"),
        Language("or", "ଓଡ଼ିଆ", "(Odia)") // Added from your activity list
    )
}

