package com.example.aasra.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(repository: MapRepository = remember { MapRepository() }) {
    var shelters by remember { mutableStateOf(emptyList<com.example.aasra.model.Shelter>()) }
    var safePoints by remember { mutableStateOf(emptyList<com.example.aasra.model.SafePoint>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            repository.loadMapData()
                .onSuccess { (s, sp, _) ->
                    shelters = s
                    safePoints = sp
                }
                .onFailure { e ->
                    errorMessage = e.message ?: "Couldn't load map data."
                }
        }
    }

    val defaultCenter = LatLng(13.0827, 80.2707) // Chennai fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 11f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        shelters.forEach { shelter ->
            Marker(
                state = MarkerState(position = LatLng(shelter.latitude, shelter.longitude)),
                title = shelter.name,
                snippet = if (shelter.isFull) "Full" else "${shelter.spaceLeft} spaces left"
            )
        }
        safePoints.forEach { point ->
            Marker(
                state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                title = point.name,
                snippet = point.type
            )
        }
    }
    // errorMessage isn't shown on the map itself yet — you could surface it as
    // a Snackbar/Toast at the call site if you want the user to see load failures.
}