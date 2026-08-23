package com.example.aasra.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aasra.auth.AuthManager
import com.example.aasra.data.FirebaseRepository
import com.example.aasra.model.Shelter
import com.example.aasra.state.AppState
import kotlinx.coroutines.launch

@Composable
fun AuthorityShelterUpdateScreen(
    repository: FirebaseRepository = remember { FirebaseRepository() },
    authManager: AuthManager = remember { AuthManager() }
) {
    var shelters by remember { mutableStateOf(emptyList<Shelter>()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSeeding by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        isLoading = true
        errorMessage = null
        scope.launch {
            repository.getShelters()
                .onSuccess { shelters = it.filter { s -> s.category == "shelter" } }
                .onFailure { e -> errorMessage = e.message ?: "Couldn't load shelters." }
            isLoading = false
        }
    }

    fun seedDemoData() {
        isSeeding = true
        errorMessage = null
        val lat = AppState.userLatitude.value ?: 13.0827
        val lon = AppState.userLongitude.value ?: 80.2707
        scope.launch {
            repository.seedDemoShelters(lat, lon)
                .onSuccess { load() }
                .onFailure { e -> errorMessage = e.message ?: "Couldn't seed demo shelters." }
            isSeeding = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Update Shelter Capacity & Supplies", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Changes are visible to everyone immediately.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("⚠ $errorMessage", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { load() }) { Text("Retry") }
                }
            }
            shelters.isEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("No shelters found yet.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { seedDemoData() }, enabled = !isSeeding) {
                        if (isSeeding) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isSeeding) "Seeding…" else "Seed demo shelters")
                    }
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shelters, key = { it.id }) { shelter ->
                        ShelterUpdateCard(
                            shelter = shelter,
                            isExpanded = expandedId == shelter.id,
                            onToggleExpand = {
                                expandedId = if (expandedId == shelter.id) null else shelter.id
                            },
                            onSave = { occupancy, food, water, medicine, toilets ->
                                scope.launch {
                                    repository.updateShelterDetails(
                                        shelterId = shelter.id,
                                        newOccupancy = occupancy,
                                        hasFood = food,
                                        hasWater = water,
                                        hasMedicine = medicine,
                                        hasToilets = toilets,
                                        role = if (authManager.isAuthority) "authority" else "volunteer"
                                    ).onSuccess {
                                        expandedId = null
                                        load()
                                    }.onFailure { e -> errorMessage = e.message }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShelterUpdateCard(
    shelter: Shelter,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSave: (occupancy: Int, food: Boolean, water: Boolean, medicine: Boolean, toilets: Boolean) -> Unit
) {
    var occupancyText by remember(shelter.id) { mutableStateOf(shelter.currentOccupancy.toString()) }
    var hasFood by remember(shelter.id) { mutableStateOf(shelter.hasFood) }
    var hasWater by remember(shelter.id) { mutableStateOf(shelter.hasWater) }
    var hasMedicine by remember(shelter.id) { mutableStateOf(shelter.hasMedicine) }
    var hasToilets by remember(shelter.id) { mutableStateOf(shelter.hasToilets) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        shelter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${shelter.currentOccupancy}/${shelter.capacity} occupied",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(if (isExpanded) "Close" else "Edit")
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = occupancyText,
                    onValueChange = { occupancyText = it.filter { c -> c.isDigit() } },
                    label = { Text("Current occupancy (capacity: ${shelter.capacity})") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                SupplyToggleRow("Food available", hasFood) { hasFood = it }
                SupplyToggleRow("Water available", hasWater) { hasWater = it }
                SupplyToggleRow("Medicine available", hasMedicine) { hasMedicine = it }
                SupplyToggleRow("Toilets available", hasToilets) { hasToilets = it }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val occupancy = occupancyText.toIntOrNull() ?: shelter.currentOccupancy
                        onSave(occupancy.coerceAtLeast(0), hasFood, hasWater, hasMedicine, hasToilets)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Update")
                }
            }
        }
    }
}

@Composable
private fun SupplyToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}