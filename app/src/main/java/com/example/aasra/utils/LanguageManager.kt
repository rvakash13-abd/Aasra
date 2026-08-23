package com.example.aasra.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Also nudges the system-level per-app locale (affects RTL layout for Urdu, etc). */
object LanguageManager {
    fun setAppLanguage(languageCode: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }
}