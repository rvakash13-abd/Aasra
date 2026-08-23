package com.example.aasra.model

data class Shelter(
    val id: String = "",
    val name: String = "",
    val landmark: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val capacity: Int = 0,
    val currentOccupancy: Int = 0,
    val hasFood: Boolean = false,
    val hasWater: Boolean = false,
    val hasMedicine: Boolean = false,
    val hasToilets: Boolean = false,
    val womenChildSupport: Boolean = false,
    val accessibilitySupport: Boolean = false,
    val verifiedByRole: String = "unverified", // "authority" | "volunteer" | "unverified"
    val lastUpdatedMillis: Long = 0L,
    val isStale: Boolean = false,
    val category: String = "shelter" // "shelter" | "shop" | "health_center"
) {
    val isFull: Boolean get() = capacity > 0 && currentOccupancy >= capacity
    val spaceLeft: Int get() = (capacity - currentOccupancy).coerceAtLeast(0)
}