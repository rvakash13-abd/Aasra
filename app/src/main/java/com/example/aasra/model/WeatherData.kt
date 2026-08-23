package com.example.aasra.model

data class WeatherData(
    val locationName: String = "",
    val temperatureC: Double = 0.0,
    val rainfallMm: Double = 0.0,
    val windSpeedKmh: Double = 0.0,
    val condition: String = "",
    val floodRiskLevel: String = "low" // "low" | "moderate" | "high" | "severe"
)