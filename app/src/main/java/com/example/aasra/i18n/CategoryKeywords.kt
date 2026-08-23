package com.example.aasra.i18n

import java.text.Normalizer

/**
 * Maps spoken/typed words in any supported language to a resource category.
 *
 * Indian-language speech recognizers frequently return text in a different
 * Unicode normalization form (decomposed vowel signs / matras) than what's
 * typed directly into source code (composed form). Two strings that render
 * identically on screen — like "தங்குமிடங்கள்" from voice vs. the same word
 * typed here — can be different byte sequences underneath, so a plain
 * String.contains() silently fails. Normalizing both sides to NFC before
 * comparing, and stripping invisible joiner characters some keyboards/STT
 * engines insert, fixes that.
 */
object CategoryKeywords {

    private val shelterKeywords = listOf(
        "shelter", "shelters",
        "आश्रय", "शरण",
        "தங்குமிட", "தங்குமிடம்", "தங்குமிடங்கள்",
        "ఆశ్రయం", "ఆశ్రయాలు",
        "ಆಶ್ರಯ",
        "അഭയകേന്ദ്രം", "അഭയം",
        "আশ্রয়",
        "आश्रयस्थान",
        "આશ્રય",
        "ਪਨਾਹ", "ਪਨਾਹਗਾਹ",
        "پناہ گاہ", "پناہ"
    )

    private val shopKeywords = listOf(
        "shop", "shops", "ration", "ration shop", "store", "supplies",
        "दुकान", "राशन", "राशन की दुकान",
        "கடை", "ரேஷன் கடை", "ரேஷன்",
        "దుకాణం", "రేషన్", "రేషన్ షాప్",
        "ಅಂಗಡಿ", "ರೇಷನ್",
        "കട", "റേഷൻ",
        "দোকান", "রেশন",
        "दुकान",
        "દુકાન", "રેશન",
        "ਦੁਕਾਨ", "ਰਾਸ਼ਨ",
        "دکان", "راشن"
    )

    private val healthKeywords = listOf(
        "health", "hospital", "health center", "health centre", "clinic",
        "अस्पताल", "स्वास्थ्य केंद्र", "स्वास्थ्य",
        "மருத்துவமனை", "சுகாதார மையம்", "மருத்துவ",
        "ఆసుపత్రి", "ఆరోగ్య కేంద్రం", "ఆరోగ్య",
        "ಆಸ್ಪತ್ರೆ", "ಆರೋಗ್ಯ ಕೇಂದ್ರ",
        "ആശുപത്രി", "ആരോഗ്യ കേന്ദ്രം",
        "হাসপাতাল", "স্বাস্থ্য",
        "रुग्णालय", "आरोग्य",
        "હોસ્પિટલ", "આરોગ્ય",
        "ਹਸਪਤਾਲ", "ਸਿਹਤ",
        "ہسپتال", "صحت"
    )

    /**
     * Strips zero-width joiners/non-joiners and variation selectors that
     * some IMEs and speech engines insert, then normalizes to NFC so
     * visually-identical text compares equal regardless of source.
     */
    private fun normalize(text: String): String {
        val stripped = text.replace(Regex("[\u200B-\u200F\uFEFF]"), "")
        return Normalizer.normalize(stripped, Normalizer.Form.NFC).trim().lowercase()
    }

    /**
     * Returns "shelter" / "shop" / "health_center" if the query (in any
     * supported language) refers to that category, or null if it doesn't
     * match a known category — in which case the caller should fall back
     * to a normal text search.
     */
    fun matchCategory(rawQuery: String): String? {
        val query = normalize(rawQuery)
        if (query.isEmpty()) return null

        fun anyMatch(keywords: List<String>) = keywords.any { kw ->
            val k = normalize(kw)
            k.isNotEmpty() && (query.contains(k) || k.contains(query))
        }

        return when {
            anyMatch(shelterKeywords) -> "shelter"
            anyMatch(shopKeywords) -> "shop"
            anyMatch(healthKeywords) -> "health_center"
            else -> null
        }
    }
}