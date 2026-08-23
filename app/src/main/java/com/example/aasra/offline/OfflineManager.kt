package com.example.aasra.offline

import android.content.Context
import com.example.aasra.model.Shelter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Caches whatever the last successful Firestore shelter list was, so the
 * app (and the Storm Alert generator) has something real to show even with
 * zero connectivity. Falls back further to DemoDataProvider if nothing has
 * ever been cached.
 */
class OfflineManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("aasra_offline_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun cacheShelters(shelters: List<Shelter>) {
        prefs.edit()
            .putString(KEY_SHELTERS, gson.toJson(shelters))
            .putLong(KEY_CACHED_AT, System.currentTimeMillis())
            .apply()
    }

    fun getCachedShelters(): List<Shelter> {
        val json = prefs.getString(KEY_SHELTERS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Shelter>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun lastCacheTimeMillis(): Long = prefs.getLong(KEY_CACHED_AT, 0L)

    companion object {
        private const val KEY_SHELTERS = "cached_shelters_v2"
        private const val KEY_CACHED_AT = "cached_at"
    }
}