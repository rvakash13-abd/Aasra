package com.example.aasra.data

import com.example.aasra.model.BlockedZone
import com.example.aasra.model.RescueRequest
import com.example.aasra.model.SafePoint
import com.example.aasra.model.Shelter
import com.example.aasra.offline.DemoDataProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()
    private val sheltersRef = db.collection("shelters")
    private val rescueRef = db.collection("rescue_requests")
    private val safePointsRef = db.collection("safe_points")
    private val blockedZonesRef = db.collection("blocked_zones")
    private val safeStatusRef = db.collection("safe_status")

    suspend fun getShelters(): Result<List<Shelter>> = runCatching {
        val snapshot = sheltersRef.get().await()
        snapshot.documents.mapNotNull { it.toObject(Shelter::class.java) }
    }

    suspend fun updateShelterDetails(
        shelterId: String,
        newOccupancy: Int,
        hasFood: Boolean,
        hasWater: Boolean,
        hasMedicine: Boolean,
        hasToilets: Boolean,
        role: String
    ): Result<Unit> = runCatching {
        sheltersRef.document(shelterId).update(
            mapOf(
                "currentOccupancy" to newOccupancy,
                "hasFood" to hasFood,
                "hasWater" to hasWater,
                "hasMedicine" to hasMedicine,
                "hasToilets" to hasToilets,
                "verifiedByRole" to role,
                "lastUpdatedMillis" to System.currentTimeMillis(),
                "isStale" to false
            )
        ).await()
        Unit
    }

    suspend fun seedDemoShelters(centerLat: Double, centerLon: Double): Result<Unit> = runCatching {
        val demo = DemoDataProvider.nearbyDemoShelters(centerLat, centerLon)
        demo.forEach { shelter -> sheltersRef.document(shelter.id).set(shelter).await() }
    }

    suspend fun submitRescueRequest(request: RescueRequest): Result<String> = runCatching {
        val doc = rescueRef.document()
        val withId = request.copy(id = doc.id, timestampMillis = System.currentTimeMillis())
        doc.set(withId).await()
        doc.id
    }

    /** Only truly-new requests — used where "pending" specifically matters. */
    suspend fun getRescueQueue(): Result<List<RescueRequest>> = runCatching {
        val snapshot = rescueRef.whereEqualTo("status", "pending").get().await()
        snapshot.documents.mapNotNull { it.toObject(RescueRequest::class.java) }
            .sortedByDescending { it.priorityScore }
    }

    /**
     * Pending + acknowledged + in_progress — i.e. everything that isn't
     * resolved yet. This is what the Authority queue screen uses, so a
     * request stays visible (with a waiting badge) after "Acknowledge" is
     * pressed, right up until the citizen taps "I Am Safe" and an authority
     * closes it out.
     */
    suspend fun getActiveRescueRequests(): Result<List<RescueRequest>> = runCatching {
        val snapshot = rescueRef.get().await()
        snapshot.documents.mapNotNull { it.toObject(RescueRequest::class.java) }
            .filter { it.status != "resolved" }
            .sortedByDescending { it.priorityScore }
    }

    suspend fun updateRescueStatus(requestId: String, status: String): Result<Unit> = runCatching {
        rescueRef.document(requestId).update("status", status).await()
        Unit
    }

    /** Reads the same "safe_status/{userId}" doc that SafeStatusScreen writes to. */
    suspend fun getSafeStatus(userId: String): Result<Boolean> = runCatching {
        val doc = safeStatusRef.document(userId).get().await()
        doc.getBoolean("isSafe") ?: false
    }

    suspend fun getSafePoints(): Result<List<SafePoint>> = runCatching {
        val snapshot = safePointsRef.get().await()
        snapshot.documents.mapNotNull { it.toObject(SafePoint::class.java) }
    }

    suspend fun getBlockedZones(): Result<List<BlockedZone>> = runCatching {
        val snapshot = blockedZonesRef.get().await()
        snapshot.documents.mapNotNull { it.toObject(BlockedZone::class.java) }
    }
}