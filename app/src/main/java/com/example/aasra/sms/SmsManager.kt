package com.example.aasra.sms

import android.telephony.SmsManager

object AasraSmsManager {

    fun sendRescueRequestSms(phoneNumber: String, latitude: Double, longitude: Double) {
        val message = "AASRA RESCUE REQUEST. Location: $latitude,$longitude. Please respond."
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    fun sendShelterStatusSms(phoneNumber: String, shelterName: String, spaceLeft: Int) {
        val message = "AASRA: $shelterName has $spaceLeft spaces left."
        SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null)
    }

    /** Parses inbound SMS like "OCC SHELTER123 45" -> shelterId to occupancy. */
    fun parseOccupancyUpdate(smsBody: String): Pair<String, Int>? {
        val parts = smsBody.trim().split(" ")
        if (parts.size != 3 || parts[0] != "OCC") return null
        val occupancy = parts[2].toIntOrNull() ?: return null
        return parts[1] to occupancy
    }
}