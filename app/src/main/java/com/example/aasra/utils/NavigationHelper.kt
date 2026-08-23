package com.example.aasra.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.aasra.model.Shelter

object NavigationHelper {
    /** Opens turn-by-turn directions to the shelter in Google Maps, falling
     *  back to a generic geo: intent if Maps isn't installed. */
    fun navigateTo(context: Context, shelter: Shelter) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${shelter.latitude},${shelter.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.gms")
        }
        try {
            context.startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            val label = Uri.encode(shelter.name)
            val geoUri = Uri.parse("geo:${shelter.latitude},${shelter.longitude}?q=${shelter.latitude},${shelter.longitude}($label)")
            context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        }
    }
}