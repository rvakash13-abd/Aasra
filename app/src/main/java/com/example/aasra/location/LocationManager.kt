package com.example.aasra.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class AasraLocationManager(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Returns null (never throws) if permission is missing or the location
     * fetch fails for any reason — callers should treat null as "location
     * unavailable" and fall back gracefully (e.g. let the user pick manually).
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLatLng(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        return try {
            val location = fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()
            location?.let { it.latitude to it.longitude }
        } catch (e: Exception) {
            null
        }
    }
}
