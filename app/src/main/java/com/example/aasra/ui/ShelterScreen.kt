package com.example.aasra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aasra.data.FirebaseRepository
import com.example.aasra.location.LocationUtils
import com.example.aasra.model.Shelter
import com.example.aasra.offline.DemoDataProvider
import com.example.aasra.offline.OfflineManager
import com.example.aasra.state.AppState
import com.example.aasra.ui.theme.AasraAmber
import com.example.aasra.ui.theme.AasraGreen
import com.example.aasra.ui.theme.AasraStale
import com.example.aasra.utils.NavigationHelper
import kotlinx.coroutines.launch

@Composable
fun ShelterScreen(repository: FirebaseRepository = remember { FirebaseRepository() }) {
    val context = LocalContext.current
    val offlineManager = remember { OfflineManager(context) }
    var shelters by remember { mutableStateOf(emptyList<Shelter>()) }
    var isLoading by remember { mutableStateOf(true) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("all") }
    val scope = rememberCoroutineScope()

    val userLat = AppState.userLatitude.value ?: 13.0827
    val userLon = AppState.userLongitude.value ?: 80.2707

    fun load() {
        isLoading = true
        infoMessage = null
        scope.launch {
            val live = repository.getShelters().getOrNull()?.takeIf { it.isNotEmpty() }
            shelters = if (live != null) {
                offlineManager.cacheShelters(live)
                live
            } else {
                val cached = offlineManager.getCachedShelters()
                if (cached.isNotEmpty()) {
                    infoMessage = "Showing offline data saved earlier."
                    cached
                } else {
                    infoMessage = "Showing demo shelters for this area."
                    DemoDataProvider.nearbyDemoShelters(userLat, userLon)
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Nearby Shelters & Resources", style = MaterialTheme.typography.headlineSmall)
        infoMessage?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryChip("All", selectedCategory == "all") { selectedCategory = "all" }
            CategoryChip("Shelters", selectedCategory == "shelter") { selectedCategory = "shelter" }
            CategoryChip("Shops", selectedCategory == "shop") { selectedCategory = "shop" }
            CategoryChip("Health", selectedCategory == "health_center") { selectedCategory = "health_center" }
        }
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            shelters.isEmpty() -> Text("No shelters listed yet.")
            else -> {
                val sorted = shelters
                    .filter { selectedCategory == "all" || it.category == selectedCategory }
                    .sortedBy { LocationUtils.distanceKm(userLat, userLon, it.latitude, it.longitude) }

                if (sorted.isEmpty()) {
                    Text("Nothing in this category nearby.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(sorted) { shelter ->
                            val distanceKm = LocationUtils.distanceKm(userLat, userLon, shelter.latitude, shelter.longitude)
                            ShelterCard(shelter, distanceKm)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

@Composable
fun ShelterCard(shelter: Shelter, distanceKm: Double) {
    val context = LocalContext.current
    val (icon, label, accent) = when (shelter.category) {
        "shop" -> Triple(Icons.Default.Storefront, "Shop", Color(0xFF6D4C41))
        "health_center" -> Triple(Icons.Default.LocalHospital, "Health Center", Color(0xFF1565C0))
        else -> Triple(Icons.Default.Home, "Shelter", AasraGreen)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = accent)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(shelter.name, style = MaterialTheme.typography.titleMedium)
                    Text(shelter.landmark, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                AssistChip(onClick = {}, label = { Text("%.1f km".format(distanceKm)) })
            }

            if (shelter.category == "shelter" && shelter.capacity > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val ratio = (shelter.currentOccupancy.toFloat() / shelter.capacity).coerceIn(0f, 1f)
                val barColor = when {
                    shelter.isFull -> MaterialTheme.colorScheme.error
                    ratio > 0.7f -> AasraAmber
                    else -> AasraGreen
                }
                LinearProgressIndicator(
                    progress = ratio,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = barColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (shelter.isFull) "Full" else "${shelter.spaceLeft} of ${shelter.capacity} spaces left",
                    color = barColor,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (shelter.category != "shelter") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, color = accent, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
            }

            if (shelter.isStale) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("⚠ Data may be outdated", style = MaterialTheme.typography.labelSmall, color = AasraStale)
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { NavigationHelper.navigateTo(context, shelter) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Navigate")
            }
        }
    }
}