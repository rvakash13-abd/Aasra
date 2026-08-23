package com.example.aasra.i18n

data class AppLanguage(val code: String, val englishName: String, val nativeName: String)

object SupportedLanguages {
    val all = listOf(
        AppLanguage("en", "English", "English"),
        AppLanguage("hi", "Hindi", "हिन्दी"),
        AppLanguage("ta", "Tamil", "தமிழ்"),
        AppLanguage("te", "Telugu", "తెలుగు"),
        AppLanguage("kn", "Kannada", "ಕನ್ನಡ"),
        AppLanguage("ml", "Malayalam", "മലയാളം"),
        AppLanguage("bn", "Bengali", "বাংলা"),
        AppLanguage("mr", "Marathi", "मराठी"),
        AppLanguage("gu", "Gujarati", "ગુજરાતી"),
        AppLanguage("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        AppLanguage("ur", "Urdu", "اردو")
    )
}