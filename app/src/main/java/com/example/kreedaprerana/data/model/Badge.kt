package com.example.kreedaprerana.data.model

/**
 * Represents a badge/achievement stored in Firestore "badges" collection.
 * Badges are auto-assigned based on performance benchmarks.
 */
data class Badge(
    val badgeId: String = "",
    val athleteId: String = "",
    val badgeName: String = "",
    val level: String = "",
    val coachId: String = "",
    val earnedDate: Long = System.currentTimeMillis()
)
