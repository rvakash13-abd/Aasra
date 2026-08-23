package com.example.aasra.utils

import com.example.aasra.model.RescueRequest

object RiskCalculator {

    /** Higher score = higher rescue priority. */
    fun computePriority(request: RescueRequest): Int {
        var score = 0
        if (request.hasInjury) score += 40
        if (request.hasElderlyOrChild) score += 25
        if (request.hasDisability) score += 20
        if (request.exposureRisk) score += 15
        return score
    }

    fun isShelterStale(lastUpdatedMillis: Long, thresholdMinutes: Long = 60): Boolean {
        val ageMinutes = (System.currentTimeMillis() - lastUpdatedMillis) / 60000
        return ageMinutes > thresholdMinutes
    }

    fun resolveConflict(
        officialOccupancy: Int?,
        officialTimestamp: Long,
        volunteerOccupancy: Int?,
        volunteerTimestamp: Long
    ): Pair<Int?, Boolean> {
        // Official always wins if present.
        if (officialOccupancy != null) return officialOccupancy to false
        if (volunteerOccupancy != null) return volunteerOccupancy to false
        return null to true // no data -> capacity uncertain
    }
}