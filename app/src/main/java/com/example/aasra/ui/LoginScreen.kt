package com.example.aasra.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aasra.auth.AuthManager
import com.example.aasra.i18n.Strings
import com.example.aasra.location.AasraLocationManager
import com.example.aasra.state.AppState
import com.example.aasra.ui.components.LanguagePickerButton
import com.example.aasra.ui.theme.AasraGreen
import com.example.aasra.ui.theme.AasraGreenDark
import kotlinx.coroutines.launch

private enum class LoginMode { CITIZEN, AUTHORITY }

@Composable
fun LoginScreen(
    onCitizenReady: () -> Unit,
    onAuthorityLoginSuccess: () -> Unit,
    authManager: AuthManager = remember { AuthManager() }
) {
    val context = LocalContext.current
    val locationManager = remember { AasraLocationManager(context) }
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(LoginMode.CITIZEN) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var manualArea by remember { mutableStateOf("") }
    var detectedLat by remember { mutableStateOf<Double?>(null) }
    var detectedLon by remember { mutableStateOf<Double?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var citizenError by remember { mutableStateOf<String?>(null) }
    var isContinuing by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }
    var authorityError by remember { mutableStateOf<String?>(null) }

    fun detectLocation() {
        isLocating = true
        citizenError = null
        scope.launch {
            val coords = locationManager.getCurrentLatLng()
            isLocating = false
            if (coords == null) citizenError = "Couldn't get GPS location. You can type your area instead."
            else { detectedLat = coords.first; detectedLon = coords.second }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) detectLocation() else citizenError = "Location permission denied. You can type your area instead."
    }

    fun continueAsCitizen() {
        citizenError = null
        if (detectedLat == null && manualArea.isBlank()) { citizenError = "Share your location or type your area to continue."; return }
        isContinuing = true
        scope.launch {
            val signedIn = authManager.ensureSignedIn()
            isContinuing = false
            if (!signedIn) { citizenError = "Couldn't connect. Check your internet and try again."; return@launch }
            val lat = detectedLat ?: 13.0827
            val lon = detectedLon ?: 80.2707
            val label = manualArea.ifBlank { "Current location" }
            AppState.setLocation(lat, lon, label)
            if (phone.isNotBlank()) AppState.userPhone.value = phone.trim()
            onCitizenReady()
        }
    }

    fun signInAuthority() {
        authorityError = null
        isLoggingIn = true
        scope.launch {
            val result = authManager.signInAuthority(email.trim(), password)
            isLoggingIn = false
            result.onSuccess {
                AppState.userEmail.value = email.trim()
                onAuthorityLoginSuccess()
            }.onFailure { e -> authorityError = e.message ?: "Login failed" }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(AasraGreen, AasraGreenDark))).padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(Strings.get("app_name"), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    LanguagePickerButton()
                }
                Text(Strings.get("app_tagline"), color = Color.White.copy(alpha = 0.9f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFEFEFEF), RoundedCornerShape(12.dp)).padding(4.dp)
            ) {
                SegmentButton(Strings.get("login_need_help"), selected = mode == LoginMode.CITIZEN, modifier = Modifier.weight(1f)) { mode = LoginMode.CITIZEN }
                SegmentButton(Strings.get("login_authority"), selected = mode == LoginMode.AUTHORITY, modifier = Modifier.weight(1f)) { mode = LoginMode.AUTHORITY }
            }

            if (mode == LoginMode.CITIZEN) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Your name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone number (optional, for SMS alerts)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
                )

                Text("Your location", fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { if (locationManager.hasLocationPermission()) detectLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    enabled = !isLocating, modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLocating) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else { Icon(Icons.Default.LocationOn, contentDescription = null); Spacer(modifier = Modifier.width(8.dp)); Text("Use my current location") }
                }
                if (detectedLat != null) {
                    Text("Location detected ✓ (${"%.4f".format(detectedLat)}, ${"%.4f".format(detectedLon)})", color = AasraGreen, fontSize = 13.sp)
                }

                Text("or type your area / locality", fontSize = 13.sp, color = Color.Gray)
                OutlinedTextField(value = manualArea, onValueChange = { manualArea = it }, label = { Text("Area, locality or landmark") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                citizenError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

                Button(onClick = { continueAsCitizen() }, enabled = !isContinuing, modifier = Modifier.fillMaxWidth()) {
                    if (isContinuing) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text(Strings.get("login_continue"))
                }
            } else {
                Text("Sign in with the credentials issued to your organization.", fontSize = 13.sp, color = Color.Gray)
                OutlinedTextField(value = email, onValueChange = { email = it; authorityError = null }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = password, onValueChange = { password = it; authorityError = null },
                    label = { Text("Password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
                )
                authorityError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                Button(
                    onClick = { signInAuthority() },
                    enabled = !isLoggingIn && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoggingIn) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text(Strings.get("login_sign_in"))
                }
            }
        }
    }
}

@Composable
private fun SegmentButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) AasraGreen else Color.Transparent
    val fg = if (selected) Color.White else Color.Black
    Box(
        modifier = modifier.background(bg, RoundedCornerShape(10.dp)).clickable { onClick() }.padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}