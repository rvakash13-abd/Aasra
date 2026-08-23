package com.example.aasra.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aasra.auth.AuthManager
import com.example.aasra.data.FirebaseRepository
import com.example.aasra.i18n.Strings
import com.example.aasra.location.AasraLocationManager
import com.example.aasra.model.RescueRequest
import com.example.aasra.state.AppState
import com.example.aasra.ui.components.VoiceSearchField
import com.example.aasra.utils.RiskCalculator
import kotlinx.coroutines.launch

@Composable
fun RescueScreen(
    repository: FirebaseRepository = remember { FirebaseRepository() },
    authManager: AuthManager = remember { AuthManager() }
) {
    val context = LocalContext.current
    val locationManager = remember { AasraLocationManager(context) }

    var landmark by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(AppState.userPhone.value ?: "") }
    var hasInjury by remember { mutableStateOf(false) }
    var hasElderlyOrChild by remember { mutableStateOf(false) }
    var hasDisability by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        errorMessage = null
        isSubmitting = true
        scope.launch {
            val coords = locationManager.getCurrentLatLng()

            val request = RescueRequest(
                userId = authManager.currentUserId ?: "anonymous",
                phoneNumber = phone.trim(),
                latitude = coords?.first ?: (AppState.userLatitude.value ?: 0.0),
                longitude = coords?.second ?: (AppState.userLongitude.value ?: 0.0),
                landmarkDescription = landmark,
                hasInjury = hasInjury,
                hasElderlyOrChild = hasElderlyOrChild,
                hasDisability = hasDisability
            )
            val prioritized = request.copy(priorityScore = RiskCalculator.computePriority(request))

            repository.submitRescueRequest(prioritized)
                .onSuccess {
                    if (phone.isNotBlank()) AppState.userPhone.value = phone.trim()
                    submitted = true
                }
                .onFailure { e -> errorMessage = e.message ?: "Couldn't send the request. Try again." }

            isSubmitting = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> submit() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(Strings.get("rescue_title"), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        VoiceSearchField(
            value = landmark,
            onValueChange = { landmark = it },
            label = Strings.get("rescue_landmark_label"),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(Strings.get("rescue_phone_label")) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        CheckboxRow("Injury present", hasInjury) { hasInjury = it }
        CheckboxRow("Elderly or child present", hasElderlyOrChild) { hasElderlyOrChild = it }
        CheckboxRow("Disability / mobility issue", hasDisability) { hasDisability = it }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("⚠ $it", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = !isSubmitting,
            onClick = {
                if (locationManager.hasLocationPermission()) submit()
                else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text(Strings.get("rescue_send"))
        }

        if (submitted) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Request sent. You'll get an SMS the moment it's acknowledged.")
        }
    }
}

@Composable
fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}