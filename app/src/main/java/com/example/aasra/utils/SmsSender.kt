package com.example.aasra.utils

import android.telephony.SmsManager
import com.example.aasra.location.LocationUtils
import com.example.aasra.model.Shelter

object SmsSender {

    fun buildShelterSms(userLat: Double, userLon: Double, shelters: List<Shelter>): String {
        val sorted = shelters
            .sortedBy { LocationUtils.distanceKm(userLat, userLon, it.latitude, it.longitude) }
            .take(3)

        return buildString {
            append("AASRA nearby help: ")
            sorted.forEachIndexed { index, s ->
                val distance = LocationUtils.distanceKm(userLat, userLon, s.latitude, s.longitude)
                val statusPart = if (s.category == "shelter") {
                    if (s.isFull) " (FULL)" else " (${s.spaceLeft} left)"
                } else ""
                append("${s.name} ${"%.1f".format(distance)}km$statusPart")
                append(" Map:https://maps.google.com/?q=${s.latitude},${s.longitude}")
                if (index != sorted.lastIndex) append(" | ")
            }
        }
    }

    /** Sent to the citizen the moment an authority presses "Acknowledge". */
    fun buildAcknowledgmentSms(nearestHelpPointName: String?): String {
        val base = "AASRA: Your rescue request has been received and ACKNOWLEDGED. " +
                "Help is on the way. Emergency numbers - Police: 100, Ambulance: 108, Fire: 101, " +
                "Disaster Helpline: 1078."
        return if (!nearestHelpPointName.isNullOrBlank()) {
            "$base Nearest help point: $nearestHelpPointName."
        } else base
    }

    /** Caller must already hold SEND_SMS permission. */
    fun sendDirect(phoneNumber: String, message: String) {
        val smsManager = SmsManager.getDefault()
        val parts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
    }
}