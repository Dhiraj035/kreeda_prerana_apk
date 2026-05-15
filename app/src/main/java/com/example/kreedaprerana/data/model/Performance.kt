package com.example.kreedaprerana.data.model

/**
 * Represents a single performance record stored in Firestore "performances" collection.
 * sprintTime is in seconds (e.g., 12.34), distance is in meters.
 */
data class Performance(
    val performanceId: String = "",
    val athleteId: String = "",
    val sprintTime: Double = 0.0,
    val distance: Double = 0.0,
    val activityType: String = "",
    val notes: String = "",
    val coachId: String = "",
    val date: Long = System.currentTimeMillis()
)
