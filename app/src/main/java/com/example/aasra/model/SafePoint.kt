package com.example.aasra.model

data class SafePoint(
    val id: String = "",
    val name: String = "",
    val type: String = "", // "school" | "community_hall" | "health_center" | "elevated_building"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val notes: String = ""
)