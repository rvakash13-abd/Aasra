package com.example.aasra.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.aasra.data.FirebaseRepository
import com.example.aasra.i18n.Strings
import com.example.aasra.model.Shelter
import com.example.aasra.offline.DemoDataProvider
import com.example.aasra.offline.OfflineManager
import com.example.aasra.state.AppState
import com.example.aasra.ui.components.LanguagePickerButton
import com.example.aasra.ui.theme.AasraAmber
import com.example.aasra.ui.theme.AasraGreen
import com.example.aasra.ui.theme.AasraGreenDark
import com.example.aasra.utils.EmailSender
import com.example.aasra.utils.SmsSender
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onFindShelter: () -> Unit,
    onRequestRescue: () -> Unit,
    onMarkSafe: () -> Unit,
    repository: FirebaseRepository = remember { FirebaseRepository() }
) {
    val context = LocalContext.current
    val offlineManager = remember { OfflineManager(context) }
    val scope = rememberCoroutineScope()

    var isGeneratingAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var isSendingSms by remember { mutableStateOf(false) }
    var smsMessage by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf(AppState.userPhone.value ?: "") }

    val lat = AppState.userLatitude.value
    val lon = AppState.userLongitude.value
    val addressLabel = AppState.userAddressLabel.value

    suspend fun resolveNearbyShelters(userLat: Double, userLon: Double): List<Shelter> {
        val live = repository.getShelters().getOrNull()?.takeIf { it.isNotEmpty() }
        if (live != null) { offlineManager.cacheShelters(live); return live }
        val cached = offlineManager.getCachedShelters()
        if (cached.isNotEmpty()) return cached
        return DemoDataProvider.nearbyDemoShelters(userLat, userLon)
    }

    fun runEmailAlert() {
        val userLat = lat ?: 13.0827
        val userLon = lon ?: 80.2707
        isGeneratingAlert = true
        alertMessage = null
        scope.launch {
            val shelters = resolveNearbyShelters(userLat, userLon)
            EmailSender.sendNearbySheltersByEmail(context, AppState.userEmail.value, userLat, userLon, shelters)
            alertMessage = "${shelters.size} nearby points added to your email draft."
            isGeneratingAlert = false
        }
    }

    fun performSmsSend() {
        val userLat = lat ?: 13.0827
        val userLon = lon ?: 80.2707
        isSendingSms = true
        smsMessage = null
        scope.launch {
            val shelters = resolveNearbyShelters(userLat, userLon)
            val text = SmsSender.buildShelterSms(userLat, userLon, shelters)
            try {
                SmsSender.sendDirect(phoneNumber.trim(), text)
                AppState.userPhone.value = phoneNumber.trim()
                smsMessage = "SMS sent to ${phoneNumber.trim()}."
            } catch (e: Exception) {
                smsMessage = "Couldn't send SMS: ${e.message}"
            }
            isSendingSms = false
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) performSmsSend() else smsMessage = "SMS permission is needed to send this offline." }

    fun runSmsAlert() {
        if (phoneNumber.isBlank()) { smsMessage = "Enter a phone number first."; return }
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) performSmsSend() else smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(AasraGreen, AasraGreenDark))).padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(Strings.get("app_name"), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    LanguagePickerButton()
                }
                Text(Strings.get("app_tagline"), color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AasraAmber)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(addressLabel.ifBlank { "Location not set" }, color = Color.White, fontSize = 14.sp)
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AasraAmber.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Thunderstorm, contentDescription = null, tint = AasraAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.get("storm_alert_title"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Send the nearest shelters, shops and health centers to your email, or straight to your phone by SMS.")
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; smsMessage = null },
                        label = { Text("Phone number for SMS") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { runEmailAlert() },
                            enabled = !isGeneratingAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = AasraAmber, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isGeneratingAlert) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black)
                            else { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(6.dp)); Text(Strings.get("btn_email")) }
                        }
                        Button(
                            onClick = { runSmsAlert() },
                            enabled = !isSendingSms,
                            colors = ButtonDefaults.buttonColors(containerColor = AasraGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSendingSms) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            else { Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(6.dp)); Text(Strings.get("btn_sms")) }
                        }
                    }

                    alertMessage?.let { Spacer(modifier = Modifier.height(8.dp)); Text(it, color = AasraGreenDark, fontSize = 13.sp) }
                    smsMessage?.let { Spacer(modifier = Modifier.height(4.dp)); Text(it, color = AasraGreenDark, fontSize = 13.sp) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ActionCard(Strings.get("btn_find_shelter"), Icons.Default.Home, AasraGreen, Modifier.weight(1f), onFindShelter)
                ActionCard(Strings.get("btn_request_rescue"), Icons.Default.Warning, Color(0xFFC62828), Modifier.weight(1f), onRequestRescue)
            }
            ActionCard(Strings.get("btn_mark_safe"), Icons.Default.CheckCircle, AasraGreen, Modifier.fillMaxWidth(), onMarkSafe)
        }
    }
}

@Composable
private fun ActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(110.dp).clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}