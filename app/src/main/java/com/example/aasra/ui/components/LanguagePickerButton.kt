package com.example.aasra.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.aasra.i18n.SupportedLanguages
import com.example.aasra.state.AppState
import com.example.aasra.utils.LanguageManager
import androidx.compose.foundation.layout.Box

@Composable
fun LanguagePickerButton(tint: Color = Color.White) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = "Change language", tint = tint)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SupportedLanguages.all.forEach { lang ->
                DropdownMenuItem(
                    text = { Text("${lang.nativeName}  (${lang.englishName})") },
                    onClick = {
                        AppState.languageCode.value = lang.code
                        LanguageManager.setAppLanguage(lang.code)
                        expanded = false
                    }
                )
            }
        }
    }
}