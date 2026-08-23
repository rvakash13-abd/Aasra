package com.example.aasra.model

data class BlockedZone(
    val id: String = "",
    val description: String = "",
    val centerLatitude: Double = 0.0,
    val centerLongitude: Double = 0.0,
    val radiusMeters: Double = 500.0,
    val reason: String = "", // "flooded_road" | "collapsed_structure" | "confirmed_hazard"
    val confirmedByAuthority: Boolean = false
)