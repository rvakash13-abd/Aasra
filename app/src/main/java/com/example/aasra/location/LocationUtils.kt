package com.example.aasra.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {
    /** Returns distance in kilometers using the Haversine formula. */
    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun isInsideBlockedZone(
        lat: Double, lon: Double,
        zoneCenterLat: Double, zoneCenterLon: Double, radiusMeters: Double
    ): Boolean {
        val distanceMeters = distanceKm(lat, lon, zoneCenterLat, zoneCenterLon) * 1000
        return distanceMeters <= radiusMeters
    }
}