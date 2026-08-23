package com.example.aasra.offline

import com.example.aasra.model.Shelter

/**
 * Generates 4–5 demo shelters plus demo shops and a health center, placed
 * within roughly 0.5–2 km of whatever coordinates you pass in. This is what
 * fixes the "no shelter nearby" message during demos — it's not tied to a
 * fixed city, it always builds points relative to the given location.
 */
object DemoDataProvider {
    fun nearbyDemoShelters(centerLat: Double, centerLon: Double): List<Shelter> {
        val now = System.currentTimeMillis()
        return listOf(
            Shelter(
                id = "demo_shelter_1",
                name = "Govt. Higher Secondary School Shelter",
                landmark = "Near Bus Depot",
                latitude = centerLat + 0.006, longitude = centerLon + 0.004,
                capacity = 150, currentOccupancy = 62,
                hasFood = true, hasWater = true, hasMedicine = true, hasToilets = true,
                womenChildSupport = true, verifiedByRole = "authority",
                lastUpdatedMillis = now, category = "shelter"
            ),
            Shelter(
                id = "demo_shelter_2",
                name = "Community Hall Relief Camp",
                landmark = "Opposite Water Tank",
                latitude = centerLat - 0.008, longitude = centerLon + 0.010,
                capacity = 100, currentOccupancy = 100,
                hasFood = true, hasWater = true, hasToilets = true,
                womenChildSupport = true, verifiedByRole = "volunteer",
                lastUpdatedMillis = now - 40 * 60 * 1000, category = "shelter"
            ),
            Shelter(
                id = "demo_shelter_3",
                name = "St. Mary's Church Relief Centre",
                landmark = "Main Road",
                latitude = centerLat + 0.012, longitude = centerLon - 0.007,
                capacity = 80, currentOccupancy = 20,
                hasFood = true, hasWater = true, hasMedicine = true, hasToilets = true,
                accessibilitySupport = true, verifiedByRole = "authority",
                lastUpdatedMillis = now, category = "shelter"
            ),
            Shelter(
                id = "demo_shelter_4",
                name = "Municipal Marriage Hall",
                landmark = "Near Railway Station",
                latitude = centerLat - 0.015, longitude = centerLon - 0.005,
                capacity = 200, currentOccupancy = 145,
                hasFood = true, hasWater = true, hasMedicine = true, hasToilets = true,
                womenChildSupport = true, accessibilitySupport = true,
                verifiedByRole = "authority", lastUpdatedMillis = now, category = "shelter"
            ),
            Shelter(
                id = "demo_health_1",
                name = "Primary Health Centre",
                landmark = "Next to Panchayat Office",
                latitude = centerLat + 0.003, longitude = centerLon - 0.012,
                hasMedicine = true, hasWater = true,
                verifiedByRole = "authority", lastUpdatedMillis = now, category = "health_center"
            ),
            Shelter(
                id = "demo_shop_1",
                name = "Ration Shop – Fair Price",
                landmark = "Market Street",
                latitude = centerLat - 0.004, longitude = centerLon + 0.006,
                hasFood = true, hasWater = true,
                verifiedByRole = "volunteer", lastUpdatedMillis = now, category = "shop"
            ),
            Shelter(
                id = "demo_shop_2",
                name = "General Store & Supplies",
                landmark = "Near Bus Stand",
                latitude = centerLat + 0.009, longitude = centerLon + 0.013,
                hasFood = true,
                verifiedByRole = "volunteer", lastUpdatedMillis = now, category = "shop"
            )
        )
    }
}