package com.example.aasra.model

data class RescueRequest(
    val id: String = "",
    val userId: String = "",
    val phoneNumber: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val landmarkDescription: String = "",
    val hasInjury: Boolean = false,
    val hasElderlyOrChild: Boolean = false,
    val hasDisability: Boolean = false,
    val exposureRisk: Boolean = false,
    val status: String = "pending", // "pending" | "acknowledged" | "in_progress" | "resolved"
    val priorityScore: Int = 0,
    val timestampMillis: Long = 0L
)