package com.example.aasra.state

import androidx.compose.runtime.mutableStateOf

object AppState {
    val userEmail = mutableStateOf<String?>(null)
    val userPhone = mutableStateOf<String?>(null)
    val userLatitude = mutableStateOf<Double?>(null)
    val userLongitude = mutableStateOf<Double?>(null)
    val userAddressLabel = mutableStateOf("")

    /** ISO language code: en, hi, ta, te, kn, ml, bn, mr, gu, pa, ur */
    val languageCode = mutableStateOf("en")

    fun setLocation(lat: Double, lon: Double, label: String) {
        userLatitude.value = lat
        userLongitude.value = lon
        userAddressLabel.value = label
    }
}