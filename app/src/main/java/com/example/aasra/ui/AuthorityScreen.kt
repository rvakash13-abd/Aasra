package com.example.aasra.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.aasra.auth.AuthManager
import com.example.aasra.data.FirebaseRepository
import com.example.aasra.i18n.Strings
import com.example.aasra.location.LocationUtils
import com.example.aasra.model.RescueRequest
import com.example.aasra.model.Shelter
import com.example.aasra.utils.SmsSender
import kotlinx.coroutines.launch

@Composable
fun AuthorityScreen(
    onNotAuthorized: () -> Unit,
    repository: FirebaseRepository = remember { FirebaseRepository() },
    authManager: AuthManager = remember { AuthManager() }
) {
    val context = LocalContext.current
    var queue by remember { mutableStateOf(emptyList<RescueRequest>()) }
    var shelters by remember { mutableStateOf(emptyList<Shelter>()) }
    var safeStatusMap by remember { mutableStateOf(mapOf<String, Boolean>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pendingAckRequest by remember { mutableStateOf<RescueRequest?>(null) }
    val scope = rememberCoroutineScope()

    fun refreshSafeStatuses(requests: List<RescueRequest>) {
        scope.launch {
            val map = mutableMapOf<String, Boolean>()
            requests.forEach { req ->
                if (req.userId.isNotBlank() && req.userId != "anonymous") {
                    map[req.userId] = repository.getSafeStatus(req.userId).getOrNull() ?: false
                }
            }
            safeStatusMap = map
        }
    }

    fun load() {
        isLoading = true
        errorMessage = null
        scope.launch {
            repository.getActiveRescueRequests()
                .onSuccess { queue = it; refreshSafeStatuses(it) }
                .onFailure { e -> errorMessage = e.message ?: "Couldn't load the queue." }
            repository.getShelters().onSuccess { shelters = it }
            isLoading = false
        }
    }

    fun sendAckSms(request: RescueRequest) {
        if (request.phoneNumber.isBlank()) return
        val nearest = shelters
            .filter { it.category == "shelter" }
            .minByOrNull { LocationUtils.distanceKm(request.latitude, request.longitude, it.latitude, it.longitude) }
        val text = SmsSender.buildAcknowledgmentSms(nearest?.name)
        try {
            SmsSender.sendDirect(request.phoneNumber, text)
        } catch (e: Exception) {
            errorMessage = "Acknowledged, but the SMS failed to send: ${e.message}"
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val req = pendingAckRequest
        pendingAckRequest = null
        if (granted && req != null) sendAckSms(req)
    }

    fun acknowledge(request: RescueRequest) {
        scope.launch {
            repository.updateRescueStatus(request.id, "acknowledged")
                .onSuccess {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.SEND_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) sendAckSms(request)
                    else {
                        pendingAckRequest = request
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                    load()
                }
                .onFailure { e -> errorMessage = e.message }
        }
    }

    fun resolve(request: RescueRequest) {
        scope.launch {
            repository.updateRescueStatus(request.id, "resolved")
                .onSuccess { load() }
                .onFailure { e -> errorMessage = e.message }
        }
    }

    LaunchedEffect(Unit) {
        if (!authManager.isAuthority) onNotAuthorized() else load()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(Strings.get("authority_queue_title"), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Requests stay listed until the person confirms they're safe.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("⚠ $errorMessage", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { load() }) { Text("Retry") }
                }
            }
            queue.isEmpty() -> Text("No pending rescue requests.")
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(queue, key = { it.id }) { request ->
                        val isSafe = safeStatusMap[request.userId] == true
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Priority: ${request.priorityScore}", style = MaterialTheme.typography.titleMedium)
                                Text(request.landmarkDescription)
                                Text("Injury: ${request.hasInjury} | Elderly/Child: ${request.hasElderlyOrChild}")
                                if (request.phoneNumber.isNotBlank()) {
                                    Text("Phone: ${request.phoneNumber}", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    if (isSafe) Strings.get("authority_marked_safe") else Strings.get("authority_waiting_safe"),
                                    color = if (isSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { acknowledge(request) },
                                        enabled = request.status == "pending"
                                    ) {
                                        Text(if (request.status == "pending") Strings.get("authority_acknowledge") else "Acknowledged ✓")
                                    }
                                    if (request.status == "acknowledged") {
                                        OutlinedButton(
                                            onClick = { resolve(request) },
                                            enabled = isSafe
                                        ) {
                                            Text("Mark Resolved")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}