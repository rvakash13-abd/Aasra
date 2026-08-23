package com.example.aasra.offline

data class CachedShelter(
    val id: String,
    val name: String,
    val landmark: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val currentOccupancy: Int,
    val cachedAtMillis: Long
)