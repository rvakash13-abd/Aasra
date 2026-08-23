package com.example.aasra.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aasra.data.FirebaseRepository
import com.example.aasra.i18n.Strings
import com.example.aasra.location.LocationUtils
import com.example.aasra.model.Shelter
import com.example.aasra.offline.DemoDataProvider
import com.example.aasra.offline.OfflineManager
import com.example.aasra.state.AppState
import com.example.aasra.ui.components.VoiceSearchField
import com.example.aasra.ui.theme.AasraAmber
import com.example.aasra.ui.theme.AasraGreen
import com.example.aasra.ui.theme.AasraGreenDark
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun LocalityScreen(repository: FirebaseRepository = remember { FirebaseRepository() }) {
    val context = LocalContext.current
    val offlineManager = remember { OfflineManager(context) }
    var resources by remember { mutableStateOf(emptyList<Shelter>()) }
    var isLoading by remember { mutableStateOf(true) }
    var infoMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val userLat = AppState.userLatitude.value ?: 13.0827
    val userLon = AppState.userLongitude.value ?: 80.2707
    val addressLabel = AppState.userAddressLabel.value

    fun load() {
        isLoading = true
        infoMessage = null
        scope.launch {
            val live = repository.getShelters().getOrNull()?.takeIf { it.isNotEmpty() }
            resources = if (live != null) {
                offlineManager.cacheShelters(live)
                live
            } else {
                val cached = offlineManager.getCachedShelters()
                if (cached.isNotEmpty()) {
                    infoMessage = "offline data"
                    cached
                } else {
                    infoMessage = "demo data"
                    DemoDataProvider.nearbyDemoShelters(userLat, userLon)
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(userLat, userLon), 13f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(AasraGreen, AasraGreenDark)))
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AasraAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            addressLabel.ifBlank { "Your locality" },
                            color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Live shelters, shops & health centers around you",
                        color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp
                    )
                }
                Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Color.White)
            }
        }

        // Status banner
        Surface(color = if (isLoading) Color(0xFFF5F5F5) else AasraGreen.copy(alpha = 0.08f)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetching latest data…", color = Color.Gray, fontSize = 13.sp)
                } else {
                    val suffix = infoMessage?.let { " (showing $it)" } ?: ""
                    Text("${resources.size} resources found nearby$suffix", fontSize = 13.sp, color = Color.DarkGray)
                }
            }
        }

        // Map preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
                Marker(
                    state = MarkerState(position = LatLng(userLat, userLon)),
                    title = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
                resources.forEach { r ->
                    val hue = when (r.category) {
                        "shop" -> BitmapDescriptorFactory.HUE_ORANGE
                        "health_center" -> BitmapDescriptorFactory.HUE_VIOLET
                        else -> if (r.isFull) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
                    }
                    Marker(
                        state = MarkerState(position = LatLng(r.latitude, r.longitude)),
                        title = r.name,
                        snippet = if (r.category == "shelter") {
                            if (r.isFull) "Full" else "${r.spaceLeft} spaces left"
                        } else r.category.replace("_", " "),
                        icon = BitmapDescriptorFactory.defaultMarker(hue)
                    )
                }
            }
        }

        // Search bar
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            VoiceSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = Strings.get("locality_search_hint"),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Category filter
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedCategory == "all", onClick = { selectedCategory = "all" }, label = { Text("All") })
                FilterChip(selected = selectedCategory == "shelter", onClick = { selectedCategory = "shelter" }, label = { Text("Shelters") })
                FilterChip(selected = selectedCategory == "shop", onClick = { selectedCategory = "shop" }, label = { Text("Shops") })
                FilterChip(selected = selectedCategory == "health_center", onClick = { selectedCategory = "health_center" }, label = { Text("Health") })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filtered = resources
            .filter { selectedCategory == "all" || it.category == selectedCategory }
            .filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.landmark.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { LocationUtils.distanceKm(userLat, userLon, it.latitude, it.longitude) }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { r ->
                val distanceKm = LocationUtils.distanceKm(userLat, userLon, r.latitude, r.longitude)
                ShelterCard(r, distanceKm) // reuses the card from ShelterScreen.kt — same package
            }
        }
    }
}