package com.example.aasra.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.aasra.location.LocationUtils
import com.example.aasra.model.Shelter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailSender {
    fun sendNearbySheltersByEmail(
        context: Context,
        toEmail: String?,
        userLat: Double,
        userLon: Double,
        shelters: List<Shelter>
    ) {
        val sorted = shelters
            .sortedBy { LocationUtils.distanceKm(userLat, userLon, it.latitude, it.longitude) }
            .take(8)

        val body = buildString {
            appendLine("AASRA Storm Alert — Nearby help points")
            appendLine("Generated: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}")
            appendLine()
            sorted.forEach { s ->
                val distance = LocationUtils.distanceKm(userLat, userLon, s.latitude, s.longitude)
                val kindLabel = when (s.category) {
                    "shop" -> "Shop / Supplies"
                    "health_center" -> "Health Center"
                    else -> "Shelter"
                }
                appendLine("• ${s.name} [$kindLabel] — ${"%.1f".format(distance)} km away")
                appendLine("   ${s.landmark}")
                if (s.category == "shelter") {
                    appendLine("   ${if (s.isFull) "FULL" else "${s.spaceLeft} spaces left"}")
                }
                appendLine("   Directions: https://www.google.com/maps/dir/?api=1&destination=${s.latitude},${s.longitude}")
                appendLine()
            }
            appendLine("This list was generated from the most recent data AASRA had, which may include offline cached information.")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, if (!toEmail.isNullOrBlank()) arrayOf(toEmail) else arrayOf())
            putExtra(Intent.EXTRA_SUBJECT, "AASRA: Shelters near you")
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            context.startActivity(Intent.createChooser(intent, "Send shelter list via"))
        }
    }
}