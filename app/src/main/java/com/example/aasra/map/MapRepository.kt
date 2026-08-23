package com.example.aasra.map

import com.example.aasra.data.FirebaseRepository
import com.example.aasra.model.BlockedZone
import com.example.aasra.model.SafePoint
import com.example.aasra.model.Shelter

class MapRepository(private val firebaseRepository: FirebaseRepository = FirebaseRepository()) {

    suspend fun loadMapData(): Result<Triple<List<Shelter>, List<SafePoint>, List<BlockedZone>>> {
        val shelters = firebaseRepository.getShelters().getOrElse { return Result.failure(it) }
        val safePoints = firebaseRepository.getSafePoints().getOrElse { return Result.failure(it) }
        val blockedZones = firebaseRepository.getBlockedZones().getOrElse { return Result.failure(it) }
        return Result.success(Triple(shelters, safePoints, blockedZones))
    }
}