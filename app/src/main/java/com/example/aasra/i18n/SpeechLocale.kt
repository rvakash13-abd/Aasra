package com.example.aasra.i18n

/**
 * Maps AASRA's in-app language code to the BCP-47 tag Android's speech
 * recognizer expects (e.g. "ta-IN" for Tamil). This is what was missing —
 * without an explicit tag, the recognizer falls back to the phone's system
 * language regardless of what's selected inside the app.
 */
object SpeechLocale {
    private val tagByLanguageCode = mapOf(
        "en" to "en-IN",
        "hi" to "hi-IN",
        "ta" to "ta-IN",
        "te" to "te-IN",
        "kn" to "kn-IN",
        "ml" to "ml-IN",
        "bn" to "bn-IN",
        "mr" to "mr-IN",
        "gu" to "gu-IN",
        "pa" to "pa-IN",
        "ur" to "ur-IN"
    )

    fun bcp47Tag(appLanguageCode: String): String =
        tagByLanguageCode[appLanguageCode] ?: "en-IN"
}