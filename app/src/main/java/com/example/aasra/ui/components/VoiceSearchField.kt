package com.example.aasra.ui.components

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.aasra.i18n.SpeechLocale
import com.example.aasra.state.AppState

/**
 * A text field with a mic button that fills it via Android's speech
 * recognizer, using AASRA's currently selected in-app language — not the
 * phone's system language — so switching to Tamil/Telugu/Hindi/etc. inside
 * the app actually changes what the mic listens for.
 */
@Composable
fun VoiceSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recognitionError by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onValueChange(spoken)
                recognitionError = null
            }
        } else {
            recognitionError = "Didn't catch that — try again, or type it in."
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    val languageTag = SpeechLocale.bcp47Tag(AppState.languageCode.value)
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        // Explicit BCP-47 tag as a String — this is the actual fix.
                        // Previously this passed Locale.getDefault() (the phone's
                        // system language), so it ignored whatever you picked in
                        // AASRA's own language switcher.
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now…")
                    }
                    try {
                        recognitionError = null
                        launcher.launch(intent)
                    } catch (e: Exception) {
                        recognitionError = "No speech recognizer available on this device."
                    }
                }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice search")
                }
            }
        )
        recognitionError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
    }
}